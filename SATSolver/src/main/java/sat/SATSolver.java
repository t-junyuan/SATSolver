package sat;


import immutable.ImList;
import sat.env.*;
import immutable.EmptyImList;
import sat.formula.*;


/**
 * A simple DPLL SAT solver. See http://en.wikipedia.org/wiki/DPLL_algorithm
 */
public class SATSolver {
    /**
     * Solve the problem using a simple version of DPLL with backtracking and
     * unit propagation. The returned environment binds literals of class
     * bool.Variable rather than the special literals used in classification of
     * class clausal.Literal, so that clients can more readily use it.
     *
     * @return an environment for which the problem evaluates to Bool.TRUE, or
     * null if no such environment exists.
     */
    public static Environment solve(Formula formula) {
        Environment e = new Environment();

        ImList<Clause> clauses = formula.getClauses();

        Environment result = solve(clauses, e);
        return result;
    }


    /**
     * Takes a partial assignment of variables to values, and recursively
     * searches for a complete satisfying assignment.
     *
     * @param clauses formula in conjunctive normal form
     * @param env     assignment of some or all variables in clauses to true or
     *                false values.
     * @return an environment for which all the clauses evaluate to Bool.TRUE,
     * or null if no such environment exists.
     */
    public static Clause smallest(ImList<Clause> clauses){
        Clause c = clauses.first();
        for (Clause x : clauses){
            if (x.size() < c.size()){
                c = x;
            }
            if (x.isEmpty()){ // if empty clause is found, means it failed so we needa backtrackkk
                return null;
            }
        }
        return c;
    }

    private static Environment solve(ImList<Clause> clauses, Environment env) {

        // if there are no more clauses, means there's a solution!
        if (clauses.isEmpty())
            return env;

        Clause smallest = smallest(clauses);

        if (smallest == null)
            return null;

        Literal l = smallest.chooseLiteral();

        if (smallest.isUnit()) { //one literal left
            return LASTONE(env, clauses, l); //checK THE LAST LITERAL AND WE'RE D O N E :-)
        } else {

            // must try for both postive and negative
            // set l as positive, then substitute, wtv substitue return is the remaining clause list if l is posititve

            // NOTE : for Substitute, for the literal assigned, if the entire clause is True, remove entire clause, but if entire clause is not true, only remove literal
            // example,  [NOT A, B],[NOT A, NOT B]
            // first loop: assign A as True ---> Both Clauses not satisfied, only remove literal A
            // next loop: [B],[NOT B] ----> put into solver again, assign B is True, only remove clause [B], but only remove literal from [NOT B]
            // so remaining clause would be [] which is empty, and will return null cos unsolvable.

            // using that list, put into solve again.
            // try to find the smallest clause in the new clause list
            // if there is an empty clause means you have taken out all the literals but still haven removed the clause
            // so it returns null. if smallest == null return null (didn't solve)
            // will succeed and return when clauses.isEmpty()
            Environment testP = env.putTrue(l.getVariable());
            Literal lP = PosLiteral.make(l.getVariable());
            ImList posclause = substitute(clauses, lP);
            Environment poslit = solve(posclause, testP);


            if (poslit == null) { // ok it didn't work time to be negative (empty clause found)
                Environment testN = env.putFalse(l.getVariable());
                Literal lN = NegLiteral.make(l.getVariable());
                ImList negClause = substitute(clauses, lN);
                Environment neglit = solve(negClause, testN);
                return neglit;
            }

            return poslit; // return env not null
        }
    }

    private static Environment LASTONE(Environment env, ImList<Clause> clauses, Literal l){
        // just clear the variable according to its negation
        if (l instanceof PosLiteral){
            env = env.put(l.getVariable(), Bool.TRUE);
            ImList<Clause> newC = substitute(clauses,l);
            return solve(newC, env);
        } else {
            env = env.put(l.getVariable(), Bool.FALSE);
            ImList<Clause> newC = substitute(clauses,l);
            return solve(newC, env);
        }
    }



    /**
     * given a clause list and literal, produce a new list resulting from
     * setting that literal to true
     *
     * @param clauses , a list of clauses
     * @param l       , a literal to set to true
     * @return a new list of clauses resulting from setting l to true
     */
    private static ImList<Clause> substitute(ImList<Clause> clauses,
                                             Literal l) {
        ImList<Clause> new_clauses = new EmptyImList<>();

        for (Clause c : clauses) {
            if (l != null) {
                Clause new_c = c.reduce(l); // set literal to true or false
                if (new_c != null) {
                    new_clauses = new_clauses.add(new_c); // append to new clauses
                }
            }
        }
        return new_clauses;
    }
}
