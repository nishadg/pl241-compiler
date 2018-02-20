package Model;

public class Register extends Result {
    public int regNo;
    public Register(int regNo) {
        super(Kind.REG);
        this.regNo = regNo;
    }

    @Override
    public String toString() {
        return "R" + regNo;
    }

}
