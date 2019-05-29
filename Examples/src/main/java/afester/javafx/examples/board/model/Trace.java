package afester.javafx.examples.board.model;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A Trace is a part of a Net which has already been routed.
 * It can either be rendered as trace or as bridge. 
 */
public class Trace extends AbstractWire {

    private boolean isBridge = false;

    public Trace(AbstractNode from, AbstractNode to) {
        super(from, to);
    }


    @Override
    public Node getXML(Document doc)  {
        Element traceNode = doc.createElement("trace");
        traceNode.setAttribute("from", Integer.toString(getFrom().id));
        traceNode.setAttribute("to",   Integer.toString(getTo().id));
        traceNode.setAttribute("isBridge",  Boolean.toString(isBridge));

        return traceNode;
    }


    public void setAsBridge() {
        isBridge = true;
    }

    
    public boolean isBridge() {
        return isBridge;
    }

    
    @Override
    public String toString() {
        return String.format("Trace[%s - %s]", this.getFrom(), this.getTo());
    }
}
