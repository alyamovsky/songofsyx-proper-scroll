package properscroll;

/** Pure geometry for the panel scroller. No game dependencies so it can be unit tested. */
public final class ScrollMath {

    private ScrollMath() {
    }

    /**
     * How far the content can be scrolled. Zero when it fits, and also when it overhangs by no more
     * than {@code tolerance}: several vanilla panels are laid out to the screen height and then get an
     * extra row stacked on top, so they hang a little past the bottom edge by design.
     */
    public static int maxScroll(int contentHeight, int viewHeight, int tolerance) {
        int overflow = contentHeight - viewHeight;
        return overflow > tolerance ? overflow : 0;
    }

    public static int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        return Math.min(value, max);
    }

    /**
     * Whether an element must not be drawn. The renderer has no clipping, so elements that are
     * completely outside the view, or that would spill above the panel's top edge, are skipped.
     * Elements partially crossing the view edges are drawn and masked by the panel decorations.
     */
    public static boolean culled(int y1, int y2, int panelTop, int viewY1, int viewY2) {
        return y1 < panelTop || y2 <= viewY1 || y1 >= viewY2;
    }

    public static int thumbHeight(int viewHeight, int contentHeight, int min) {
        if (contentHeight <= viewHeight) {
            return viewHeight;
        }
        int h = (int) ((long) viewHeight * viewHeight / contentHeight);
        return clamp(h, Math.min(min, viewHeight), viewHeight);
    }

    public static int thumbOffset(int viewHeight, int thumbHeight, int scroll, int maxScroll) {
        if (maxScroll <= 0) {
            return 0;
        }
        return (int) ((long) (viewHeight - thumbHeight) * clamp(scroll, 0, maxScroll) / maxScroll);
    }
}
