package fr.irisa.diverse.printing;


import soot.*;
import soot.jimple.Stmt;
import soot.jimple.internal.*;
import soot.tagkit.AttributeValueException;
import soot.tagkit.Tag;

import java.nio.file.Path;
import java.util.HashMap;

/**
 * Prints the control flow in .Dot for GraphVis to visualize
 * <p>
 * Created by marodrig on 22/04/2015.
 */
public class GraphVisPrettyPrint {

    public static class IdTag implements Tag {

        public static String ID_TAG = "id";

        int id = 0;

        public IdTag(int id) {
            this.id = id;
        }

        @Override
        public String getName() {
            return "id";
        }

        @Override
        public byte[] getValue() throws AttributeValueException {
            return new byte[0];
        }
    }

    private final Body body;

    public GraphVisPrettyPrint(Body b) {
        this.body = b;
    }

    /**
     * Prints the control flow in .Dot for GraphVis to visualize
     */
    public void printControlFlow() {
        StringBuilder sb = new StringBuilder("digraph ").append(body.getMethod().getName()).append(" { \n");
        sb.append("start [shape=Mdiamond];\n");
        sb.append("node [fontsize = 8];\n");
        int i = 0;
        HashMap<Value, Unit> defs = new HashMap<>();
        for (Unit u : body.getUnits()) {
            sb.append(" ").append(i);
            if (u instanceof JIfStmt) sb.append(" [shape=diamond, label=\"");
            else if (u instanceof JReturnStmt || u instanceof JReturnVoidStmt) sb.append(" [label=\"");
            else sb.append(" [shape=rectangle, label=\"");

            sb.append(u.toString().replace("\"", "quot ")).append(", ").append("\"];\n");
            if ( !u.hasTag(IdTag.ID_TAG) ) u.addTag(new IdTag(i));
            i++;
            //System.out.println(u);
        }
        Unit prev = null;
        for (Unit u : body.getUnits()) {
            //Def use pairs
            for (ValueBox v : u.getDefBoxes()) defs.put(v.getValue(), u);
            for (ValueBox v : u.getUseBoxes()) {
                if (defs.containsKey(v.getValue()))
                    printRedLink(sb, defs.get(v.getValue()), u, v.getValue().toString());
            }
            //Control flow
            if (u instanceof JIfStmt) printLink(sb, u, ((JIfStmt) u).getTarget());
            else if (u instanceof JGotoStmt) printLink(sb, u, ((JGotoStmt) u).getTarget());


            if (prev != null) {
                if (!(prev instanceof JReturnStmt) &&
                        !(prev instanceof JReturnVoidStmt) && !(prev instanceof JGotoStmt))
                    printLink(sb, prev, u);
            } else {
                int toId = ((IdTag) u.getTag(IdTag.ID_TAG)).id;
                sb.append("start").append(" -> ").append(toId).append(";\n");
            }

            prev = u;
            //System.out.println(u);
        }
        sb.append("}");
        System.out.println(sb.toString());
        System.out.println("*************");
    }


    private void printRedLink(StringBuilder sb, Unit from, Unit to, String label) {
        int fromId = ((IdTag) from.getTag(IdTag.ID_TAG)).id;
        int toId = ((IdTag) to.getTag(IdTag.ID_TAG)).id;
        sb.append(fromId).append(" -> ").append(toId).append("[label=").
                append("\"").append(label).append("\", color=red, penwidth=3.0];\n");
    }

    private void printLink(StringBuilder sb, Unit from, Unit to) {
        int fromId = ((IdTag) from.getTag(IdTag.ID_TAG)).id;
        int toId = ((IdTag) to.getTag(IdTag.ID_TAG)).id;
        sb.append(fromId).append(" -> ").append(toId).append(";\n");
    }

}
