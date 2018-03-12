package pinkpanthers.pinkshelters;

/**
 * Created by hdang on 3/8/18.
 */

public enum Restrictions {
    MEN("men"),
    WOMEN("women"),
    NON_BINARY("non_binary"),
    CHILDREN("children"),
    YOUNG_ADULTS("young_adults"),
    FAMILIES_W_NEWBORNS("families_w_newborns"),
    ANYONE("anyone"),
    FAMILY("families"),
    VETERAN("veterans"),
    FAMILIES_W_CHILDREN_UNDER_5("family_w_children_under_5");

    private String sqlRestriction;
    Restrictions(String restriction) {
        this.sqlRestriction = restriction;
    }

    public String toString() {
        return sqlRestriction;
    }
}
