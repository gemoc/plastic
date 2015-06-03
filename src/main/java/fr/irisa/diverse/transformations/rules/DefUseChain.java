package fr.irisa.diverse.transformations.rules;

import soot.Unit;
import soot.ValueBox;
import soot.jimple.Stmt;
import soot.jimple.internal.JAssignStmt;

import java.util.List;

/**
 * Created by marodrig on 03/06/2015.
 */
public class DefUseChain implements Rule {

    /**
     * Indicates if two units are in a def use chain
     *
     * @param a
     * @param b
     * @return
     */
    protected boolean inDefUseChain(Unit a, Unit b) {
        for (ValueBox va : a.getUseBoxes())
            for (ValueBox vb : b.getDefBoxes())
                if (va.getValue().equals(vb.getValue())) return true;

        for (ValueBox vb : b.getUseBoxes())
            for (ValueBox va : a.getDefBoxes())
                if (va.getValue().equals(vb.getValue())) return true;

        return false;
    }

    @Override
    public boolean apply(List<Unit> units, int index) {
        if (index == 0) return false;

        Unit u = units.get(index);


        //if (u instanceof JReturnStmt || u instanceof JReturnVoidStmt) return false;

        Unit prev = units.get(index - 1);

        //Keep it to the very minimum initially
        if (!(u instanceof JAssignStmt) || !(prev instanceof JAssignStmt)) return false;

        //TODO: better analyse these cases!!!!
        //if (prev.branches()) return false;
        //if (prev instanceof JReturnStmt || prev instanceof JReturnVoidStmt) return false;

        if (inDefUseChain(prev, u)) return false;


        Stmt s = (Stmt) u;
        if (s.branches() || s.getBoxesPointingToThis().size() > 0) {
            return false;
/*
            //Control branches can't go up. The analysis is too complicated to do it before the thurday:
            //TODO: se later how to put them up

            List<Unit> targets = new ArrayList<>();

            if (u instanceof JIfStmt) targets.add(((JIfStmt) u).getTarget());
            if (u instanceof JGotoStmt) targets.add(((JGotoStmt) u).getTarget());
            if (u instanceof JTableSwitchStmt) targets.addAll(((JTableSwitchStmt) u).getTargets());

            if (targets.size() == 0) throw new RuntimeException("unknown branching statement: " + u.toString());
            //If none of the statements in the target uses a definition of the previous statement, the
            //whole control structure can go up
            for ( Unit target : targets ) {
                int i = index + 1;
                Unit t = target;
                while (i < units.size() && !t.branches()) {
                    Unit next = units.get(i);
                    if (inDefUseChain(prev, t)) return false;
                    t = next;
                    i++;
                }
            }*/
        }
        return true;
    }

}
