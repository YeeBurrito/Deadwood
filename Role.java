public class Role {
    private String roleName;
    private String roleDescription;
    private int rank;
    private String roleType;
    private int[] roleArea;

    // Returns the name of the role
    public String getRoleName() {
        return this.roleName;
    }

    // returns the type of role. either "extra" or "star"
    public String getRoleType(){
        return this.roleType;
    }

    // Returns the description of the role
    public String getRoleDescription() {
        return this.roleDescription;
    }

    // returns the rank of the role
    public int getRank() {
        return this.rank;
    }

    // Returns the area array specifying the dimensions of the role
    public int[] getArea(){
        return this.roleArea;
    }

    // Constructor
    public Role(String roleName, String roleDescription, int rank, String roleType, int[] roleArea) {
        this.roleName = roleName;
        this.roleDescription = roleDescription;
        this.rank = rank;
        this.roleType = roleType;
        this.roleArea = roleArea;
    }

    // Prints the name and rank of a role
    public void printRole() {
        System.out.print("Role " + roleName.toUpperCase() + " has rank " + rank + ". ");
    }
}
