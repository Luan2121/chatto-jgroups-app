import org.jgroups.util.Streamable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Encapsulates information about a draw command. Used by the {@link Draw} and
 * other demos.
 *
 */
public class Command implements Streamable {
    static final byte DRAW = 1;
    static final byte CLEAR = 2;
    static final byte MESSAGE = 3;
    byte mode;
    int x;
    int y;

    double red;
    double green;
    double blue;

    String message;

    public Command() { // needed for streamable
    }

    Command(byte mode) {
        this.mode = mode;
    }

    Command(byte mode, String message) {
        this.message = message;
        this.mode = mode;
    }

    Command(byte mode, int x, int y, double red, double green, double blue) {
        this.mode = mode;
        this.x = x;
        this.y = y;
        this.green = green;
        this.blue = blue;
        this.red = red;
    }

    @Override
    public void writeTo(DataOutput out) throws IOException {
        out.writeByte(mode);
        out.writeInt(x);
        out.writeInt(y);
        out.writeDouble(red);
        out.writeDouble(green);
        out.writeDouble(blue);
        out.writeUTF(message);
    }

    @Override
    public void readFrom(DataInput in) throws IOException {
        mode = in.readByte();
        x = in.readInt();
        y = in.readInt();
        red = in.readDouble();
        green = in.readDouble();
        blue = in.readDouble();
        message = in.readUTF();
    }

    public String toString() {
        StringBuilder ret = new StringBuilder();
        switch (mode) {
            case DRAW:
                ret.append("DRAW(" + x + ", " + y + ")");
                break;
            case CLEAR:
                ret.append("CLEAR");
                break;
            case MESSAGE:
                ret.append("MESSAGE: " + message);
            default:
                return "<undefined>";
        }
        return ret.toString();
    }

}