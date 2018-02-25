package RA;

import Model.Register;
import Model.Result;

public class RegisterAllocator {
    private static int registerCounter = 0;
    Register getRegister(Result x){
//        switch (x.kind){
//            case VAR:
//
//        }
        return new Register(++registerCounter);
    }
}
