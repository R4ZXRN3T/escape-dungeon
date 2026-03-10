package org.lasarimanstudios.escapedungeon.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.Disposable;

/**
 * Draws the player's money in the top-right corner with a gold coin icon.
 *
 * <p>Must be called outside any active SpriteBatch.begin()/end() and resized from the
 * screen's {@code resize()} so the HUD camera matches the window size.</p>
 */
public class MoneyHUD implements Disposable {

    private static final float DEFAULT_ICON_SIZE = 32f;
    private static final float DEFAULT_MARGIN = 16f;
    private static final float DEFAULT_PADDING = 8f;

    private final Texture coinTexture;
    private final BitmapFont font;
    private final GlyphLayout layout;
    private final OrthographicCamera hudCamera;

    public MoneyHUD() {
        coinTexture = new Texture(Gdx.files.internal("ui/money/gold_coin.png"));
        coinTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        // create a readable bitmap font from the bundled TTF
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("ui/font.ttf"));
        try {
            FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
            parameter.size = 36; // reasonable default for most screens
            parameter.magFilter = Texture.TextureFilter.Nearest;
            parameter.minFilter = Texture.TextureFilter.Nearest;
            parameter.incremental = true;
            font = generator.generateFont(parameter);
        } finally {
            generator.dispose();
        }

        layout = new GlyphLayout();
        hudCamera = new OrthographicCamera();

        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    /**
     * Updates the HUD camera to the new screen size.
     */
    public void resize(int screenWidth, int screenHeight) {
        hudCamera.setToOrtho(false, screenWidth, screenHeight);
        hudCamera.update();
    }

    /**
     * Renders the coin icon and the current money amount in the top-right corner.
     * Call this outside any active SpriteBatch.begin()/end().
     *
     * @param batch the sprite batch to use (will be begun/ended internally)
     * @param currentMoney the amount of money to display
     */
    public void render(SpriteBatch batch, int currentMoney) {
        float screenW = hudCamera.viewportWidth;
        float screenH = hudCamera.viewportHeight;

        // scale icon with screen height to keep it reasonable on very large/small screens
        float iconSize = Math.max(16f, Math.min(64f, screenH * 0.05f));
        float margin = DEFAULT_MARGIN;
        float padding = DEFAULT_PADDING;

        float coinX = screenW - margin - iconSize;
        float coinY = screenH - margin - iconSize;

        String text = String.valueOf(currentMoney);
        layout.setText(font, text);
        float textWidth = layout.width;
        float textHeight = layout.height;

        float textX = coinX - padding - textWidth;
        // place text baseline roughly vertically centered with the icon
        float textY = coinY + iconSize / 2f + textHeight / 2f;

        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();
        // draw coin
        if (coinTexture != null) {
            batch.draw(coinTexture, coinX, coinY, iconSize, iconSize);
        }
        // draw text (yellow/gold color)
        font.setColor(Color.valueOf("ffd54f")); // warm gold
        font.draw(batch, text, textX, textY);
        batch.end();
    }

    @Override
    public void dispose() {
        if (coinTexture != null) coinTexture.dispose();
        if (font != null) font.dispose();
    }
}
