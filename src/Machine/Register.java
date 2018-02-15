package Machine;

public class Register {
    private static int regCounter = 0;
    static int allocate() {
        return regCounter++;
    }

    static void deallocate(int number) {

    }
}
