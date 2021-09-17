
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import org.jgroups.*;
import org.jgroups.jmx.JmxConfigurator;
import org.jgroups.stack.AddressGenerator;
import org.jgroups.util.OneTimeAddressGenerator;
import org.jgroups.util.Util;

import javax.management.MBeanServer;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.util.List;
import java.util.*;

public class ChatController implements Receiver, ChannelListener {

    @FXML
    private TextField chatInput;
    @FXML
    private ScrollPane chatContainer;
    @FXML
    private VBox chatContent;
    @FXML
    private BorderPane whiteboard;
    @FXML
    private AnchorPane mainContainer;

    protected String cluster_name = "chat3";
    private JChannel channel = null;
    private int memberSize = 1;
    private int stateTimeout = 5000;
    private boolean jmx;
    private boolean drawing;
    private final Random random = new Random(System.currentTimeMillis());
    private Color drawingColor = selectColor();
    private final List<Address> members = new ArrayList<>();

    private String props = null;

    public ChatController() {
    }

    public ChatController(JChannel channel) throws Exception {
        this.channel = channel;
        channel.setReceiver(this);
        channel.addChannelListener(this);
    }

    public ChatController(JChannel channel, boolean use_state, long state_timeout) throws Exception {
        this.channel = channel;
        channel.setReceiver(this);
        channel.addChannelListener(this);
    }

    public String getClusterName() {
        return cluster_name;
    }

    public void initChat(String host, String user) throws Exception {
        try {
            this.initCluster();
        } catch (Error e) {

        }
    }

    public void initCluster() throws Exception {
        jmx = true;
        AddressGenerator generator = null;
        String name = null;
        channel = new JChannel(props).addAddressGenerator(generator).setName(name);
        channel.setReceiver(this).addChannelListener(this);
        channel.connect(cluster_name, null, stateTimeout);
        this.setTitle("Chatto");
    }

    private Color selectColor() {
        int red = Math.abs(random.nextInt() % 255);
        int green = Math.abs(random.nextInt() % 255);
        int blue = Math.abs(random.nextInt() % 255);
        return Color.rgb(red, green, blue);
    }

    public void handleEnterOnChatInput(KeyEvent event) throws Exception {
        if (event.getCode().toString().equals("ENTER")) {
            String newMessage = chatInput.getText();
            System.out.println(newMessage);
            Command comm = new Command(Command.MESSAGE, newMessage);
            byte[] buf = Util.streamableToByteBuffer(comm);
            sendToAll(buf);
        }
    }

    public void handleMousePressed(MouseEvent e) {
        drawing = true;
    }

    public void handleMouseMove(MouseEvent e) throws Exception {
        String eventType = e.getEventType().toString();
        System.out.println(eventType);
        if (drawing) {
            double x = e.getX();
            double y = e.getY();

            Command comm = new Command(Command.DRAW, (int) x, (int) y, drawingColor.getRed(), drawingColor.getGreen(),
                    drawingColor.getBlue());
            byte[] buf = Util.streamableToByteBuffer(comm);
            sendToAll(buf);
        }
    }

    public void handleMouseRelease(MouseEvent e) {
        drawing = false;
    }

    public void receive(Message msg) {
        byte[] buf = msg.getArray();

        if (buf == null) {
            System.err.printf("%s: received null buffer from %s, headers: %s\n", channel.getAddress(), msg.getSrc(),
                    msg.printHeaders());
            return;
        }

        try {
            Command comm = Util.streamableFromByteBuffer(Command::new, buf, msg.getOffset(), msg.getLength());
            switch (comm.mode) {
                case Command.DRAW:

                    Platform.runLater(() -> {
                        int x = comm.x;
                        int y = comm.y;
                        Color color = Color.rgb((int) comm.red, (int) comm.green, (int) comm.blue);

                        Circle newCircle = new Circle();
                        newCircle.setCenterX(x);
                        newCircle.setCenterY(y);
                        newCircle.setRadius(5.0);
                        newCircle.setFill(color);

                        whiteboard.getChildren().add(newCircle);
                    });

                    break;
                case Command.CLEAR:
                    break;
                case Command.MESSAGE:

                    Platform.runLater(() -> {
                        String newMessage = comm.message;
                        System.out.println("Mensaje recibido: " + newMessage);
                        Label label = new Label();
                        label.setText(newMessage);
                        chatContent.getChildren().add(label);
                    });

                    break;
                default:
                    System.err.println("***** received invalid draw command " + comm.mode);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void channelConnected(JChannel channel) {
        if (jmx)
            Util.registerChannel(channel, "jgroups");
    }

    public void channelDisconnected(JChannel channel) {
        if (jmx) {
            MBeanServer server = Util.getMBeanServer();
            if (server != null) {
                try {
                    JmxConfigurator.unregisterChannel(channel, server, cluster_name);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setTitle(String title) {
        if (channel.getAddress() != null) {
            String tmp = channel.getAddress() + " (" + memberSize + ")";

            Platform.runLater(() -> {
                Stage stage = (Stage) ((Scene) mainContainer.getScene()).getWindow();
                stage.setTitle(tmp);
            });

        }
    }

    public void stop() {
        try {
            channel.close();
        } catch (Exception ex) {
            System.err.println(ex);
        }
    }

    public void getState(OutputStream ostream) throws Exception {

    }

    public void setState(InputStream istream) throws Exception {

    }

    private void sendToAll(byte[] buf) throws Exception {
        for (Address mbr : members)
            channel.send(new BytesMessage(mbr, buf));
    }

    public void viewAccepted(View v) {
        memberSize = v.size();
        members.clear();
        members.addAll(v.getMembers());
        setTitle("");
        if (v instanceof MergeView) {
            System.out.println("** " + v);
        } else {
            System.out.println("** View=" + v);
        }

    }

}
