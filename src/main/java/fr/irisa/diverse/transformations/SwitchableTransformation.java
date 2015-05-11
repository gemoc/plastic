package fr.irisa.diverse.transformations;

import soot.Body;
import soot.Unit;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by marodrig on 23/04/2015.
 */
public class SwitchableTransformation extends Switchable {

    public int transfCount = 0;

    /**
     * Count how many statements can or can be switched in the body
     *
     * @param body body for the switching is going to be
     */
    @Override
    public void execute(Body body) {

        if (transfCount > 50) return;
        try {
            int lastSwitch = -1;
            List<Unit> ub = new ArrayList<>();
            ub.addAll(body.getUnits());
            for (int i = body.getMethod().getParameterCount() + 3; i < ub.size(); i++)
                if (isSwitchAble(ub, i)) lastSwitch = i;
            if (lastSwitch != -1) {
                body.getUnits().remove(ub.get(lastSwitch));
                body.getUnits().insertBefore(ub.get(lastSwitch), ub.get(lastSwitch - 1));
                //transfCount++;
                System.out.println("Body tranformed: " + body.getMethod().getName());
            }
        } catch (RuntimeException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
