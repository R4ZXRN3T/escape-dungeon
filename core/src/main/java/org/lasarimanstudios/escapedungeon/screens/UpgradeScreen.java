package org.lasarimanstudios.escapedungeon.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import org.lasarimanstudios.escapedungeon.DungeonGame;
import org.lasarimanstudios.escapedungeon.roguelike.Perk;
import org.lasarimanstudios.escapedungeon.roguelike.PlayerStats;

import java.util.List;

/**
 * Between-levels upgrade screen where the player picks one of three random perks.
 *
 * <p>After selecting a perk, the next level is loaded automatically. If no perks are available
 * (all abilities acquired and somehow no stat perks either), the next level loads immediately.</p>
 */
public class UpgradeScreen extends ScreenAdapter {

    private static final int CARD_WIDTH = 420;
    private static final int CARD_HEIGHT = 200;
    private static final int PERK_CHOICES = 3;

    private final DungeonGame game;
    private final PlayerStats playerStats;
    private final String nextMapName;
    private final int earnedMoney;

    private Stage stage;
    private Skin skin;
    private BitmapFont titleFont;
    private BitmapFont cardFont;
    private BitmapFont descFont;
    private Texture buttonBackground;

    /**
     * Creates the upgrade screen.
     *
     * @param game        game instance used to load the next level
     * @param playerStats current run stats (perks are applied here)
     * @param nextMapName map identifier for the next level
     * @param earnedMoney money earned so far (carried forward)
     */
    public UpgradeScreen(DungeonGame game, PlayerStats playerStats, String nextMapName, int earnedMoney) {
        this.game = game;
        this.playerStats = playerStats;
        this.nextMapName = nextMapName;
        this.earnedMoney = earnedMoney;
    }

    private static BitmapFont createFontFromTtf(int size) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("ui/font.ttf"));
        try {
            FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
            p.size = size;
            p.magFilter = Texture.TextureFilter.Nearest;
            p.minFilter = Texture.TextureFilter.Nearest;
            p.incremental = true;
            return generator.generateFont(p);
        } finally {
            generator.dispose();
        }
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        buttonBackground = new Texture(Gdx.files.internal("ui/buttons/button_background.png"));
        buttonBackground.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        titleFont = createFontFromTtf(100);
        cardFont = createFontFromTtf(52);
        descFont = createFontFromTtf(36);

        skin = new Skin();
        skin.add("default-font", cardFont);

        TextButton.TextButtonStyle cardStyle = new TextButton.TextButtonStyle();
        cardStyle.up = new TextureRegionDrawable(new TextureRegion(buttonBackground));
        cardStyle.down = new TextureRegionDrawable(new TextureRegion(buttonBackground));
        cardStyle.over = new TextureRegionDrawable(new TextureRegion(buttonBackground));
        cardStyle.font = cardFont;
        skin.add("default", cardStyle);

        Label.LabelStyle titleStyle = new Label.LabelStyle(titleFont, Color.WHITE);
        skin.add("title", titleStyle);

        Label.LabelStyle defaultLabelStyle = new Label.LabelStyle(cardFont, Color.WHITE);
        skin.add("default", defaultLabelStyle);

        Label.LabelStyle descStyle = new Label.LabelStyle(descFont, Color.LIGHT_GRAY);
        skin.add("desc", descStyle);

        // ── build layout ──

        Table root = new Table();
        root.setFillParent(true);
        root.center();
        stage.addActor(root);

        Label title = new Label("CHOOSE AN UPGRADE", skin, "title");
        title.setAlignment(Align.center);
        root.add(title).colspan(PERK_CHOICES).padBottom(50f).row();

        List<Perk> choices = Perk.getRandomSelection(playerStats, PERK_CHOICES);

        if (choices.isEmpty()) {
            // no perks available – go straight to next level
            proceedToNextLevel();
            return;
        }

        for (Perk perk : choices) {
            Table card = buildPerkCard(perk);
            root.add(card).width(CARD_WIDTH).height(CARD_HEIGHT).pad(20f);
        }
    }

    /**
     * Builds a clickable "card" table for a single perk.
     */
    private Table buildPerkCard(Perk perk) {
        Table card = new Table(skin);
        card.setBackground(new TextureRegionDrawable(new TextureRegion(buttonBackground)));

        Label nameLabel = new Label(perk.displayName, skin);
        nameLabel.setAlignment(Align.center);
        nameLabel.setWrap(true);

        Label descLabel = new Label(perk.description, skin, "desc");
        descLabel.setAlignment(Align.center);
        descLabel.setWrap(true);

        card.add(nameLabel).width(CARD_WIDTH - 40).padTop(15f).row();
        card.add(descLabel).width(CARD_WIDTH - 40).padTop(10f).expandY().row();

        card.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                perk.apply(playerStats);
                playerStats.acquirePerk(perk);
                proceedToNextLevel();
            }
        });

        return card;
    }

    /**
     * Loads the next level with the current run stats.
     */
    private void proceedToNextLevel() {
        game.openLevelWithStats(nextMapName, playerStats, earnedMoney);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        if (stage != null) {
            stage.getViewport().update(width, height, true);
        }
    }

    @Override
    public void hide() {
        if (Gdx.input.getInputProcessor() == stage) {
            Gdx.input.setInputProcessor(null);
        }
    }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (skin != null) skin.dispose();
        if (titleFont != null) titleFont.dispose();
        if (cardFont != null) cardFont.dispose();
        if (descFont != null) descFont.dispose();
        if (buttonBackground != null) buttonBackground.dispose();
    }
}

