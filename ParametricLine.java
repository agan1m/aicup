import java.awt.geom.Point2D;
import java.util.ArrayList;

public class ParametricLine {
    Point2D p1;
    Point2D p2;

    public ParametricLine(Point2D p1, Point2D p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    public Point2D getFraction(double frac) {
        return new Point2D.Double(p1.getX() + frac * (p2.getX() - p1.getX()), p1.getY() + frac * (p2.getY() - p1.getY()));
    }
}


