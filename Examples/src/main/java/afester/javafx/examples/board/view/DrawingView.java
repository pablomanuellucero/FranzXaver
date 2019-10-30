package afester.javafx.examples.board.view;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

// TODO: This also needs to support vertical and horizontal scrolling
public class DrawingView extends Pane { // ScrollPane {
    private double mx = 0;
    private double my = 0;
    private double scaleFactor = 3.7;
    private static final double SCALE_STEP = Math.sqrt(1.3);
    private Parent content;

    private boolean isPanning = false;
    private Cursor oldCursor;

    // TODO: Move to a central place and make it customizable
    private boolean isPanelAction(MouseEvent e) {
        return (e.isControlDown() && e.isShiftDown()); 
    }

    public DrawingView(Parent pContent) {
        this.content = pContent;

        content.setScaleX(scaleFactor);
        content.setScaleY(scaleFactor);

        Rectangle clipRectangle = new Rectangle();
        setClip(clipRectangle);
        layoutBoundsProperty().addListener((observable, oldValue, newValue) -> {
            clipRectangle.setWidth(newValue.getWidth());
            clipRectangle.setHeight(newValue.getHeight());
        });

        // Show the border of the drawing view for debugging purposes
        // setStyle("-fx-border-color: black; -fx-border-width: 1px;");

        // the Pane also has the mouse listeners for pan and zoom in order to be able to catch them at any position 
        setOnMousePressed( e-> {
            if (isPanelAction(e)) {
                // System.err.println("VIEW:" + e);

                mx = e.getX();
                my = e.getY();
                oldCursor = getCursor();
                setCursor(Cursor.CLOSED_HAND);
                isPanning = true;
            }
        });


        setOnMouseDragged(e -> {
            if (isPanning) {
                // System.err.println("VIEW:" + e);

                double dx = mx - e.getX();
                double dy = my - e.getY();
                mx = e.getX();
                my = e.getY();
                content.setLayoutX(content.getLayoutX() - dx);
                content.setLayoutY(content.getLayoutY() - dy);
            } 
        });

        setOnMouseReleased(e -> {
            if (isPanning) {
                setCursor(oldCursor);
                isPanning = false;
            }
        });

        setOnScroll(e-> {
            
            if (e.isControlDown()) {
                // System.err.println(e);

                // calculate the new scale factor
                double newScale;
                if (e.getDeltaY() < 0) {
                    newScale = scaleFactor / SCALE_STEP;
                }else {
                    newScale = scaleFactor * SCALE_STEP;
                }
                if (newScale > 0) {
                    scaleFactor = newScale;

                    // store current mouse coordinates as a reference point
                    final Point2D mPos = new Point2D(e.getSceneX(), e.getSceneY());       // scene coordinates of mouse
                    final Point2D mPosContent = content.sceneToLocal(mPos);               // position in content coordinates

                    // scale the content
                    content.setScaleX(scaleFactor);
                    content.setScaleY(scaleFactor);

                    // move the content so that the reference point is again at the mouse position
                    Point2D dviewPos = content.localToParent(mPosContent);  // reference point in dview coordinates
                    Point2D dviewMouse = sceneToLocal(mPos);                // destination point
                    Point2D diff = dviewMouse.subtract(dviewPos);
                    
                    content.setLayoutX(content.getLayoutX() + diff.getX());
                    content.setLayoutY(content.getLayoutY() + diff.getY());
                }
            }/* else if (e.isShiftDown()) {
                System.err.println("Scroll horizontally");
            } else {
                System.err.println("Scroll vertically");
            }*/
        });


        // use a ScrollPane instead
//        setHbarPolicy(ScrollBarPolicy.ALWAYS);
//        setVbarPolicy(ScrollBarPolicy.ALWAYS);
//        setPannable(true);
//        setContent(new Group(content));
        
        Rectangle r = new Rectangle();
        r.setStroke(Color.LIGHTGREY);
        r.setFill(Color.WHITE);
        content.boundsInParentProperty().addListener((obj, oldValue, newValue) -> {
            r.setLayoutX(newValue.getMinX() - 10);
            r.setLayoutY(newValue.getMinY() - 10);
            r.setWidth(newValue.getWidth() + 20);
            r.setHeight(newValue.getHeight() + 20);
        });
        getChildren().add(r);

        getChildren().add(content);
    }


    private void updateZoom() {
        System.err.printf("Factor: %s\n", scaleFactor);
        content.setScaleX(scaleFactor);
        content.setScaleY(scaleFactor);
    }


    /**
     * Centers the content pane so that it is in the center of the DrawingArea.
     */
    public void centerContent() {
        Bounds daBounds = getBoundsInLocal();
        Bounds contentBoundsInParent = content.getBoundsInParent();

        System.err.println("Bounds in DrawingView area:");
        System.err.println("  DrawingView:" + daBounds);
        System.err.println("  Content    :" + contentBoundsInParent);

        final Point2D destPointParent = new Point2D((daBounds.getWidth()  - contentBoundsInParent.getWidth() ) / 2,
                                                    (daBounds.getHeight() - contentBoundsInParent.getHeight()) / 2);
        System.err.println("  New upper left corner of content in DrawingView: " + destPointParent);

        final Point2D destPointChild = content.parentToLocal(destPointParent);
        System.err.println("  New upper left corner of content in content: " + destPointChild);

        final Point2D actualPointChild = new Point2D(content.getBoundsInLocal().getMinX(), content.getBoundsInLocal().getMinY());
        System.err.println("  Actual upper left corner of content in content: " + actualPointChild);

        final Point2D newPosChild = destPointChild.subtract(actualPointChild);
        System.err.println("  New position of zero point of content in content: " + newPosChild);

        final Point2D newPosParent = content.localToParent(newPosChild);
        System.err.println("  New position of zero point of content in parent: " + newPosParent);

        content.setLayoutX(newPosParent.getX());
        content.setLayoutY(newPosParent.getY());
    }


    /**
     * Scales and centers the content pane so that it fits into the DrawingArea.
     */
    public void fitContentToWindow() {
        Bounds daBounds = getLayoutBounds();
        Bounds contentBounds = content.getBoundsInLocal();

        System.err.println("DrawingArea:" + daBounds);
        System.err.println("Contents   :" + contentBounds);

        final double margin = 10;
        double wFactor = (daBounds.getWidth() - margin) / contentBounds.getWidth();
        double hFactor = (daBounds.getHeight() - margin) / contentBounds.getHeight();
        scaleFactor = Math.min(wFactor,  hFactor);

        updateZoom();
        centerContent();
    }
}