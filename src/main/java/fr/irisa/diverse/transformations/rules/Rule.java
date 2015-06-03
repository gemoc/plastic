package fr.irisa.diverse.transformations.rules;

import soot.Unit;
import soot.ValueBox;

import java.util.List;

/**
 * Created by marodrig on 03/06/2015.
 */
public interface Rule {

    /**
     * Indicates if a unit is switchable within the body
     *
     * @param units units of the box
     * @param index Index of the unit
     * @return True if switchable, false otherwise
     */
    public boolean apply(List<Unit> units, int index);
}
