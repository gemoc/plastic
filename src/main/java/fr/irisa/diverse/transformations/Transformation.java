package fr.irisa.diverse.transformations;

import fr.irisa.diverse.transformations.rules.Rule;
import soot.Body;
import soot.Unit;

import java.util.Collection;
import java.util.List;

/**
 * Created by marodrig on 04/05/2015.
 */
public abstract class Transformation {

    protected int numberOfTransformation = 0;

    /**
     * List of all rules we have
     */
    private Collection<Rule> rules;

    /**
     * Indicates that a unit maybe switch able within the body
     *
     * @param units units of the box
     * @param index Index of the unit
     * @return True if switchable, false otherwise
     */
    protected boolean isMaybeSwitchable(List<Unit> units, int index) {
        return false;
    }

    public Collection<Rule> getRules() {
        return rules;
    }

    public void setRules(Collection<Rule> rules) {
        this.rules = rules;
    }

    public abstract void execute(Body body);

    public int getNumberOfTransformation() {
        return numberOfTransformation;
    }
}
