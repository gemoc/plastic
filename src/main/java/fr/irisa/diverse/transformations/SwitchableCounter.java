package fr.irisa.diverse.transformations;

import soot.Body;
import soot.Unit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by marodrig on 23/04/2015.
 */
public class SwitchableCounter extends Switchable {


    public static class Counter {
        public int getTotal() {
            return total;
        }

        public int getSwitchable() {
            return switchable;
        }

        public int getMaybeSwitchable() {
            return maybeSwitchable;
        }

        /**
         * Total of Jimple statements found in the project
         */
        int total = 0;

        /**
         * Total statements that can be switched
         */
        int switchable = 0;

        /**
         * Total of statements that are probably switchable
         */
        int maybeSwitchable = 0;
    }

    Counter projectCounter = new Counter();

    HashMap<String, Counter> classCounters = new HashMap<>();

    public HashMap<String, Counter> getMethodCounters() {
        return methodCounters;
    }

    public HashMap<String, Counter> getClassCounters() {
        return classCounters;
    }

    public Counter getProjectCounter() {
        return projectCounter;
    }

    HashMap<String, Counter> methodCounters = new HashMap<>();

    /**
     * Count how many statements can or can be switched in the body
     *
     * @param body body for the switching is going to be
     */
    @Override
    public void execute(Body body) {



        String className = body.getMethod().getDeclaringClass().getName();
        Counter classCounter;
        if (!classCounters.containsKey(className)) {
            classCounter = new Counter();
            classCounters.put(className, classCounter);
        } else classCounter = classCounters.get(className);


        Counter bodyCounter = new Counter();
        bodyCounter.total = 3;
        methodCounters.put(className + "." + body.getMethod().getName(), bodyCounter);

        List<Unit> ub = new ArrayList<>();
        ub.addAll(body.getUnits());
        for (int i = body.getMethod().getParameterCount(); i < ub.size(); i++) {
            if (isSwitchAble(ub, i)) bodyCounter.switchable++;
            else if (isMaybeSwitchable(ub, i)) bodyCounter.maybeSwitchable++;
            bodyCounter.total++;
        }

        classCounter.total += bodyCounter.total;
        classCounter.switchable += bodyCounter.switchable;

        projectCounter.total += classCounter.total;
        projectCounter.switchable += classCounter.switchable;
    }

    //public void switchableBranch(Unit upUnit, )

    public void reset() {
        methodCounters = new HashMap<>();
        classCounters = new HashMap<>();
        projectCounter = new Counter();
    }
}
