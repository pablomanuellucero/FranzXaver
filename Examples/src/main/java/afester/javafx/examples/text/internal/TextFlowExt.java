package afester.javafx.examples.text.internal;

//import static org.fxmisc.richtext.TwoDimensional.Bias.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javafx.geometry.Point2D;
import javafx.scene.shape.PathElement;
import javafx.scene.text.TextFlow;

//import org.fxmisc.richtext.TwoLevelNavigator;

import afester.javafx.examples.text.CharacterHit;

import com.sun.javafx.geom.RectBounds;
import com.sun.javafx.scene.text.HitInfo;
import com.sun.javafx.scene.text.TextLayout;
import com.sun.javafx.text.PrismTextLayout;
import com.sun.javafx.text.TextLine;

/**
 * Adds additional API to {@link TextFlow}.
 */
public class TextFlowExt extends TextFlow {

    private static Method mGetTextLayout;
    private static Method mGetLines;
    private static Method mGetLineIndex;
    private static Method mGetCharCount;
    static {
        try {
            mGetTextLayout = TextFlow.class.getDeclaredMethod("getTextLayout");
            mGetLines = PrismTextLayout.class.getDeclaredMethod("getLines");
            mGetLineIndex = PrismTextLayout.class.getDeclaredMethod("getLineIndex", float.class);
            mGetCharCount = PrismTextLayout.class.getDeclaredMethod("getCharCount");
        } catch (NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }
        mGetTextLayout.setAccessible(true);
        mGetLines.setAccessible(true);
        mGetLineIndex.setAccessible(true);
        mGetCharCount.setAccessible(true);
    }

    private static Object invoke(Method m, Object obj, Object... args) {
        try {
            return m.invoke(obj, args);
        } catch (IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * @return The number of lines which are displayed in the TextFlow.
     *         This is the actual number of (physical) lines which are 
     *         visible on the screen.
     */
    public int getLineCount() {
        return getLines().length;
    }


    /**
     * @return The number of characters which are displayed in this TextFlow area.
     */
    public int getCharCount() {
        return (int) invoke(mGetCharCount, textLayout());
    }


    //int getLineOfCharacter(int charIdx) {
    //    TextLine[] lines = getLines();
    //    TwoLevelNavigator navigator = new TwoLevelNavigator(
    //            () -> lines.length,
    //            i -> lines[i].getLength());
    //    return navigator.offsetToPosition(charIdx, Forward).getMajor();
    // }

    /**
     * 
     * @param charIdx
     * @param isLeading
     * @return
     */
    public PathElement[] getCaretShape(int charIdx, boolean isLeading) {
        return textLayout().getCaretShape(charIdx, isLeading, 0.0f, 0.0f);
    }

    /**
     * 
     * @param from
     * @param to
     * @return
     */
    public PathElement[] getRangeShape(int from, int to) {
        return textLayout().getRange(from, to, TextLayout.TYPE_TEXT, 0, 0);
    }

    //CharacterHit hitLine(double x, int lineIndex) {
    //    return hit(x, getLineCenter(lineIndex));
    //}

    public CharacterHit hit(double x, double y) {
        HitInfo hit = textLayout().getHitInfo((float) x, (float) y);
        int charIdx = hit.getCharIndex();

        int lineIdx = getLineIndex((float) y);
        
        System.err.printf("   TextFlow.hit(): lineIdx=%d, charIdx=%d, lineCount=%d, charCount=%d\n", 
                          lineIdx, charIdx, getLineCount(),
                getCharCount());

        if(lineIdx >= getLineCount()) {
            return CharacterHit.insertionAt(getCharCount());
        }

        TextLine line = getLines()[lineIdx];
        RectBounds lineBounds = line.getBounds();

        if(x < lineBounds.getMinX() || x > lineBounds.getMaxX()) {
            if(hit.isLeading()) {
                return CharacterHit.insertionAt(charIdx);
            } else {
                return CharacterHit.insertionAt(charIdx + 1);
            }
        } else {
            if(hit.isLeading()) {
                return CharacterHit.leadingHalfOf(charIdx);
            } else {
                return CharacterHit.trailingHalfOf(charIdx);
            }
        }
    }

    public CharacterHit hit(Point2D pos) {
        return hit(pos.getX(), pos.getY());
    }

    private float getLineY(int index) {
        TextLine[] lines = getLines();
        float spacing = (float) getLineSpacing();
        float lineY = 0;
        for(int i = 0; i < index; ++i) {
            lineY += lines[i].getBounds().getHeight() + spacing;
        }
        return lineY;
    }

    private float getLineCenter(int index) {
        return getLineY(index) + getLines()[index].getBounds().getHeight() / 2;
    }

    private TextLine[] getLines() {
        return (TextLine[]) invoke(mGetLines, textLayout());
    }

    private int getLineIndex(float y) {
        return (int) invoke(mGetLineIndex, textLayout(), y);
    }


    private TextLayout textLayout() {
        return (TextLayout) invoke(mGetTextLayout, this);
    }
}
