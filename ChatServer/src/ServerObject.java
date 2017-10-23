import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class ServerObject {
    private ServerSocket serverSocket;
    private Thread serverThread;
    private int port;
    private static LinkedList<String> namesOfParticipants = new LinkedList<String>();

    BlockingQueue<SocketProcessor> q = new LinkedBlockingQueue<SocketProcessor>();

    public ServerObject(int port) throws IOException
    {
        serverSocket = new ServerSocket(port);
        this.port = port;
    }


    void run()
    {
        System.out.println("Wait...");
        serverThread = Thread.currentThread();
        while(true)
        {
            Socket socket = getNewConn();

            if(serverThread.isInterrupted())
            {
                break;
            }
            else if(socket != null)
            {
                try {
                    final SocketProcessor processor = new SocketProcessor(socket);
                    final Thread thread = new Thread(processor);
                    thread.setDaemon(true);
                    thread.start();
                    q.offer(processor);

                    }
                    catch(IOException ignored){}
            }
        }
    }



    private Socket getNewConn()
    {
        Socket s = null;
        try
        {
            s = serverSocket.accept();
        }
        catch (IOException e)
        {
            shutdownServer();
        }
        return s;
    }


    private synchronized void shutdownServer()
    {
        for (SocketProcessor s: q)
        {
            s.close();
        }
        if(!serverSocket.isClosed())
        {
            try
            {
                serverSocket.close();
            } catch (IOException ignored) {}
        }
    }


    public static void main(String[] args) throws IOException
    {
        new ServerObject(6666).run();
    }


    private class SocketProcessor implements Runnable
    {
        Socket s;
        BufferedReader bufferedReader;
        BufferedWriter bufferedWriter;

        SocketProcessor(Socket socketParam) throws IOException
        {
            s = socketParam;
            bufferedReader = new BufferedReader(new InputStreamReader(s.getInputStream(), "UTF-8"));
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), "UTF-8"));

        }

        public synchronized void FillAllNamesForParticipants(SocketProcessor processor, String myName){

            for(String participantsName:namesOfParticipants){
                if(!participantsName.equals(myName)){
                    processor.send(participantsName + ": ~newBody");
                    //System.out.println(myName + " -> " + participantsName);
                }
            }
        }


        public void run()
        {
            while(!s.isClosed())
            {
                String line = null;
                try
                {
                    line = bufferedReader.readLine();
                    System.out.println(line);

                    if(line.split(":")[1].equals(" ~newBody")) {
                        namesOfParticipants.add(line.split(":")[0]);
                        FillAllNamesForParticipants(this, line.split(":")[0]);

                    }
                    else if(line.split(":")[1].equals(" ~killBody")){
                        namesOfParticipants.remove(line.split(":")[0]);
                        close();
                    }
                }
                catch(IOException e)
                {
                    close();
                }

                if(line == null)
                {
                    close();
                }
                else if ("shutdown".equals(line)) {
                    serverThread.interrupt();

                    try {
                        new Socket("localhost", port);
                    } catch (IOException ignored) {
                    } finally {
                        shutdownServer();
                    }
                }
                else
                {
                    for(SocketProcessor sp:q)
                    {
                        sp.send(line);
                    }
                }
            }
        }

        public synchronized void send(String line)
        {
            try
            {
                bufferedWriter.write(line);
                bufferedWriter.write("\n");
                bufferedWriter.flush();
            }
            catch (IOException e)
            {
                close();
            }
        }


        public synchronized void close()
        {
            q.remove(this);
            if(!s.isClosed())
            {
                try
                {
                    s.close();
                }
                catch (IOException ignored){}
            }
        }


        @Override
        protected void finalize() throws Throwable
        {
            super.finalize();
            close();
        }


    }

}
