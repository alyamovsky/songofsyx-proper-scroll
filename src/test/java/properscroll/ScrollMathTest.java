package properscroll;

/** Dependency-free tests, run by build.sh with plain java. */
public final class ScrollMathTest {

    private static int failures;

    public static void main(String[] args) {
        maxScroll();
        clamp();
        culled();
        thumb();
        if (failures > 0) {
            System.err.println(failures + " test(s) failed");
            System.exit(1);
        }
        System.out.println("ScrollMathTest: all tests passed");
    }

    private static void maxScroll() {
        check(ScrollMath.maxScroll(500, 600) == 0, "content shorter than view needs no scroll");
        check(ScrollMath.maxScroll(600, 600) == 0, "content equal to view needs no scroll");
        check(ScrollMath.maxScroll(1100, 661) == 439, "overflow is content minus view");
    }

    private static void clamp() {
        check(ScrollMath.clamp(-5, 0, 100) == 0, "clamp below min");
        check(ScrollMath.clamp(150, 0, 100) == 100, "clamp above max");
        check(ScrollMath.clamp(42, 0, 100) == 42, "clamp inside range");
        check(ScrollMath.clamp(42, 0, 0) == 0, "clamp with empty range");
    }

    private static void culled() {
        int top = 51;
        int vy1 = 99;
        int vy2 = 760;
        check(!ScrollMath.culled(99, 131, top, vy1, vy2), "element at view top is drawn");
        check(!ScrollMath.culled(700, 732, top, vy1, vy2), "element inside view is drawn");
        check(!ScrollMath.culled(80, 112, top, vy1, vy2), "element crossing view top is drawn (masked by title bar)");
        check(!ScrollMath.culled(740, 772, top, vy1, vy2), "element crossing view bottom is drawn (masked by margin)");
        check(ScrollMath.culled(60, 99, top, vy1, vy2), "element fully above view is culled");
        check(ScrollMath.culled(50, 140, top, vy1, vy2), "element reaching above panel top is culled");
        check(ScrollMath.culled(760, 792, top, vy1, vy2), "element fully below view is culled");
        check(ScrollMath.culled(-300, -268, top, vy1, vy2), "element far above is culled");
    }

    private static void thumb() {
        check(ScrollMath.thumbHeight(600, 600, 16) == 600, "thumb fills track when nothing to scroll");
        check(ScrollMath.thumbHeight(600, 1200, 16) == 300, "thumb is proportional to visible share");
        check(ScrollMath.thumbHeight(600, 100000, 16) == 16, "thumb never smaller than min");
        check(ScrollMath.thumbOffset(600, 300, 0, 600) == 0, "thumb at top when not scrolled");
        check(ScrollMath.thumbOffset(600, 300, 600, 600) == 300, "thumb at bottom when fully scrolled");
        check(ScrollMath.thumbOffset(600, 300, 300, 600) == 150, "thumb halfway when half scrolled");
        check(ScrollMath.thumbOffset(600, 300, 900, 600) == 300, "thumb clamps overscroll");
        check(ScrollMath.thumbOffset(600, 600, 10, 0) == 0, "thumb offset is zero without overflow");
    }

    private static void check(boolean ok, String what) {
        if (!ok) {
            failures++;
            System.err.println("FAIL: " + what);
        }
    }
}
