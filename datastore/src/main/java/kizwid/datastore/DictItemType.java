package kizwid.datastore;

/**
 * Created by kevin on 19/08/2014.
 */
public enum DictItemType {
    Instruction(0),Collection(1),Sdos(2),Custom(3);
    private final int code;
    DictItemType(int code) {this.code = code;}
    public int getCode() {return code;}
}
