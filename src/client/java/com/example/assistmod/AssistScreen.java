package com.example.assistmod;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class AssistScreen extends Screen {
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static final int SPACING = 24;

    public AssistScreen() {
        super(Component.literal("Assist Menu"));
    }

    @Override
    protected void init() {
        int x = (this.width - BUTTON_WIDTH) / 2;
        int y = this.height / 2 - (Assist.values().length * SPACING) / 2;

        for (Assist assist : Assist.values()) {
            Button button = Button.builder(labelFor(assist), btn -> {
                        assist.toggle();
                        btn.setMessage(labelFor(assist));
                    })
                    .bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                    .tooltip(Tooltip.create(Component.literal(assist.description)))
                    .build();
            this.addRenderableWidget(button);
            y += SPACING;
        }

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, btn -> this.onClose())
                .bounds(x, y + 8, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
    }

    private static Component labelFor(Assist assist) {
        return Component.literal(assist.label + ": " + (assist.isEnabled() ? "ON" : "OFF"));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        String title = this.title.getString();
        int titleWidth = this.font.width(title);
        graphics.text(this.font, title, (this.width - titleWidth) / 2, 20, 0xFFFFFFFF, true);
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(null);
    }
}
