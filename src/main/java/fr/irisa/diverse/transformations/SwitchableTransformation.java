package fr.irisa.diverse.transformations;

import soot.Body;
import soot.Unit;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by marodrig on 23/04/2015.
 */
public class SwitchableTransformation extends Switchable {

    /**
     * Count how many statements can or can be switched in the body
     *
     * @param body body for the switching is going to be
     */
    @Override
    public void execute(Body body) {
        try {
            numberOfTransformation = 0;
            int lastSwitch = -1;
            List<Unit> ub = new ArrayList<>();
            ub.addAll(body.getUnits());
            for (int i = body.getMethod().getParameterCount() + 3; i < ub.size(); i++)
                if (isSwitchAble(ub, i)) lastSwitch = i;
            if (lastSwitch != -1) {
                Unit prev = ub.get(lastSwitch - 1);
                Unit after = ub.get(lastSwitch);
                body.getUnits().remove(after);
                body.getUnits().insertBefore(after, prev);
                numberOfTransformation++;
                System.out.println("--------------------------------------");// + " with previous: " + prev);
                System.out.println("Body tranformed: " + body.getMethod().getName());
                System.out.println("Switched ");
                System.out.println(prev); //+ " with previous: " + prev);
                System.out.println("with posterior:");// + " with previous: " + prev);
                System.out.println(after);// + " with previous: " + prev);
                System.out.println("--------------------------------------");// + " with previous: " + prev);
            }
        } catch (RuntimeException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
