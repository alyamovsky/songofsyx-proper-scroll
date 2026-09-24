package view.interrupter;

import init.constant.C;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import properscroll.ScrollMath;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.ArrayList;
import snake2d.util.sprite.SPRITE;
import util.colors.GCOLOR;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GText;
import util.text.Dic;

/**
 * Proper Scroll. Replaces the vanilla class of the same name: mod script jars precede
 * SongsOfSyx.jar on the classpath. Everything is vanilla 0.71.44 except {@link Panel},
 * which scrolls its content with the mouse wheel when it is taller than the screen.
 */
public final class ISidePanels extends Interrupter {

    /** Where Panel.set() puts the top of a panel's content. */
    private static final int VIEW_Y1 = ISidePanel.Y2 + ISidePanel.M;
    /** Pixels per mouse wheel notch. */
    private static final int SCROLL_STEP = 40;
    private static final int MIN_THUMB = 16;
    /** Culled elements are parked this far away for the duration of one render call. */
    private static final int CULL_OFFSET = 1 << 20;
    private static final COORDINATE FAR = new Coo(-CULL_OFFSET, -CULL_OFFSET);

    private final ArrayList<Panel> free = new ArrayList<Panel>(16);
    private final ArrayList<Panel> added = new ArrayList<Panel>(16);
    private final GuiSection section = new GuiSection();
    private int x2;
    private final int x1;
    private final InterManager m;

    public ISidePanels(InterManager m, int x1) {
        this.m = m;
        for (int i = 0; i < 16; i++) {
            free.add(new Panel());
        }
        this.x1 = x1 - 1;
    }

    public void add(ISidePanel panel, boolean clear) {
        add(panel, clear, false);
    }

    public void addDontRemove(ISidePanel panel, ISidePanel panel2) {
        if (added(panel)) {
            add(panel, true);
            add(panel2, false);
        } else {
            add(panel2, true);
        }
    }

    public void addDontRemove(ISidePanel panel, ISidePanel panel2, ISidePanel panel3) {
        boolean p1 = added(panel);
        boolean p2 = added(panel2);
        clear();
        if (p1) {
            add(panel, false);
        }
        if (p2) {
            add(panel2, false);
        }
        add(panel3, false);
    }

    public void toggle(ISidePanel panel, boolean clear) {
        if (added(panel)) {
            remove(panel);
        } else {
            add(panel, clear, false);
        }
    }

    public void add(ISidePanel panel, boolean clear, boolean pin) {
        if (clear) {
            remove();
        }
        for (int i = 0; i < added.size(); i++) {
            Panel p = added.get(i);
            if (p.panel == panel) {
                p.set(panel);
                rearrange();
                show(m);
                return;
            }
        }
        addP(panel, pin);
        show(m);
    }

    public void remove(ISidePanel panel) {
        for (int i = 0; i < added.size(); i++) {
            Panel p = added.get(i);
            if (p.panel == panel) {
                added.removeOrdered(i);
                free.add(p);
                rearrange();
                return;
            }
        }
    }

    @Override
    protected boolean otherClick(MButt button) {
        if (button == MButt.RIGHT && added.size() > 0) {
            for (int i = added.size() - 1; i >= 0; i--) {
                Panel p = added.get(i);
                if (p.panel.back()) {
                    return false;
                }
                if (!p.pinned) {
                    added.removeOrdered(i);
                    free.add(p);
                    rearrange();
                    return true;
                }
            }
        }
        return false;
    }

    public void clear() {
        for (Panel p : added) {
            free.add(p);
        }
        added.clear();
        rearrange();
    }

    public boolean added(ISidePanel panel) {
        if (!isActivated()) {
            return false;
        }
        for (int i = 0; i < added.size(); i++) {
            if (added.get(i).panel == panel) {
                return true;
            }
        }
        return false;
    }

    private void addP(ISidePanel panel, boolean pinned) {
        Panel p = free.removeLast();
        p.set(panel);
        added.add(p);
        rearrange();
        p.pinned = pinned;
        panel.addAction();
        panel.update(0.0f);
    }

    private void remove() {
        for (int i = 0; i < added.size(); i++) {
            Panel p = added.get(i);
            if (!p.pinned) {
                free.add(p);
                added.removeOrdered(i);
                i--;
            }
        }
    }

    private void rearrange() {
        section.clear();
        x2 = x1;
        for (Panel p : added) {
            p.panel.last = this;
            p.body().moveX1Y1(section.getLastX2(), ISidePanel.Y1);
            section.add(p);
        }
        section.body().moveX1(x1);
        section.body().moveY1(ISidePanel.Y1);
        x2 = section.body().x2();
    }

    @Override
    protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
        return section.hover(mCoo);
    }

    @Override
    protected void mouseClick(MButt button) {
        if (MButt.LEFT == button) {
            section.click();
        }
        if (MButt.RIGHT == button) {
            otherClick(button);
        }
    }

    @Override
    protected void hoverTimer(GBox text) {
        section.hoverInfoGet(text);
    }

    @Override
    protected boolean render(Renderer r, float ds) {
        if (x2 > 0) {
            addManager.viewPort().moveX1(x2);
            addManager.viewPort().setWidth(C.WIDTH() - x2);
        }
        section.render(r, ds);
        return true;
    }

    @Override
    protected boolean update(float ds) {
        for (Panel p : added) {
            p.panel.update(ds);
        }
        return true;
    }

    private static int viewY2() {
        return C.HEIGHT() - ISidePanel.M;
    }

    private final class Panel extends GuiSection {
        boolean pinned;
        private final GText title;
        private ISidePanel panel;
        private GuiSection content;
        private final CLICKABLE close;
        private int scroll;
        private final java.util.ArrayList<RENDEROBJ> parked = new java.util.ArrayList<>();

        private Panel() {
            title = new GText(UI.FONT().H2, 20).lablify();
            close = new GButt.ButtPanel(SPRITES.icons().m.exit) {
                @Override
                protected void clickA() {
                    ISidePanels.this.remove(ISidePanels.Panel.this.panel);
                }

                @Override
                public void hoverInfoGet(GUI_BOX text) {
                    text.title(Dic.¤¤Close);
                    text.add((SPRITE) text.text().add('(').add(Dic.¤¤RightClick).add(')'));
                }
            };
        }

        void set(ISidePanel panel) {
            clear();
            GuiSection s = panel.section();
            body().setHeight(C.HEIGHT() - ISidePanel.Y1);
            body().setWidth(s.body().width() + 2 * ISidePanel.M);
            body().moveY1(ISidePanel.Y1);
            s.body().centerIn(this);
            s.body().moveY1(VIEW_Y1);
            add(s);
            close.body().moveC(body().x2() - (close.body().width() / 2 + ISidePanel.M), (ISidePanel.Y1 + ISidePanel.Y2) / 2);
            add(close);
            this.panel = panel;
            content = s;
            scroll = 0;
        }

        private int overflow() {
            return ScrollMath.maxScroll(content.body().height(), viewY2() - VIEW_Y1);
        }

        @Override
        public boolean hover(COORDINATE mCoo) {
            if (overflow() == 0) {
                return super.hover(mCoo);
            }
            if (mCoo.y() < VIEW_Y1 || mCoo.y() >= viewY2()) {
                // Over the title bar or bottom margin: content scrolled under them is hidden and must not react.
                content.hover(FAR);
                content.visableSet(false);
                boolean ret = super.hover(mCoo);
                content.visableSet(true);
                return ret;
            }
            park();
            boolean ret = super.hover(mCoo);
            unpark();
            return ret;
        }

        @Override
        public void render(SPRITE_RENDERER r, float ds) {
            if (panel.title != null) {
                title.clear().add(panel.title).adjustWidth();
            }
            int x1 = body().x1();
            int x2 = body().x2();
            COLOR.WHITE10.render(r, x1, x2, ISidePanel.Y1, C.HEIGHT());
            renderContent(r, ds);
            // Title bar is drawn after the content so content scrolled under it stays hidden.
            COLOR.WHITE10.render(r, x1, x2, ISidePanel.Y1, VIEW_Y1);
            UI.PANEL().butt.render(r, x1, x2 - 3, ISidePanel.Y1 + UI.PANEL().butt.margin, ISidePanel.Y2 - UI.PANEL().butt.margin, 0, DIR.N.mask() | DIR.S.mask());
            GCOLOR.UI().border(r, x1, x1 + 3, ISidePanel.Y1, C.HEIGHT());
            GCOLOR.UI().border(r, x2 - 3, x2, ISidePanel.Y1, C.HEIGHT());
            if (title.length() != 0) {
                title.adjustWidth();
                int x = x1 + (close.body().x1() - x1) / 2;
                int y = close.body().cY();
                title.renderC(r, x, y);
            }
            close.render(r, ds);
            hoveredIs = false;
        }

        private void renderContent(SPRITE_RENDERER r, float ds) {
            int max = overflow();
            scroll = ScrollMath.clamp(scroll, 0, max);
            int y1 = VIEW_Y1 - scroll;
            if (content.body().y1() != y1) {
                content.body().moveY1(y1);
            }
            if (max == 0) {
                content.render(r, ds);
                return;
            }
            park();
            content.render(r, ds);
            unpark();
            // After the content, so scrollable widgets inside it get the wheel first.
            if (hoveredIs) {
                float d = MButt.clearWheelSpin();
                if (d != 0) {
                    scroll = ScrollMath.clamp(scroll - Math.round(d * SCROLL_STEP), 0, max);
                }
            }
            COLOR.WHITE10.render(r, body().x1(), body().x2(), viewY2(), C.HEIGHT());
            renderScrollbar(r, max);
        }

        /** The renderer cannot clip, so elements that would spill out of the panel are parked off-screen. */
        private void park() {
            int vy2 = viewY2();
            for (RENDEROBJ e : content.elements()) {
                RECTANGLE b = e.body();
                if (ScrollMath.culled(b.y1(), b.y2(), ISidePanel.Y1, VIEW_Y1, vy2)) {
                    e.body().incrY(CULL_OFFSET);
                    parked.add(e);
                }
            }
        }

        private void unpark() {
            for (RENDEROBJ e : parked) {
                e.body().incrY(-CULL_OFFSET);
            }
            parked.clear();
        }

        private void renderScrollbar(SPRITE_RENDERER r, int max) {
            int vy2 = viewY2();
            int viewHeight = vy2 - VIEW_Y1;
            int thumb = ScrollMath.thumbHeight(viewHeight, content.body().height(), MIN_THUMB);
            int y = VIEW_Y1 + ScrollMath.thumbOffset(viewHeight, thumb, scroll, max);
            int bx2 = body().x2() - 4;
            int bx1 = bx2 - 3;
            COLOR.WHITE20.render(r, bx1, bx2, VIEW_Y1, vy2);
            COLOR.WHITE65.render(r, bx1, bx2, y, y + thumb);
        }
    }
}
