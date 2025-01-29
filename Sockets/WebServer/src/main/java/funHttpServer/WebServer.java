/*
Simple Web Server in Java which allows you to call 
localhost:9000/ and show you the root.html webpage from the www/root.html folder
You can also do some other simple GET requests:
1) /random shows you a random picture (well random from the set defined)
2) json shows you the response as JSON for /random instead the html page
3) /file/filename shows you the raw file (not as HTML)
4) /multiply?num1=3&num2=4 multiplies the two inputs and responses with the result
5) /github?query=users/amehlhase316/repos (or other GitHub repo owners) will lead to receiving
   JSON which will for now only be printed in the console. See the todo below

The reading of the request is done "manually", meaning no library that helps making things a 
little easier is used. This is done so you see exactly how to pars the request and 
write a response back
*/

package funHttpServer;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.Map;
import java.util.LinkedHashMap;
import java.nio.charset.Charset;
import org.json.*;

class WebServer {
  public static void main(String args[]) {
    WebServer server = new WebServer(9000);
  }

  /**
   * Main thread
   * @param port to listen on
   */
  public WebServer(int port) {
    ServerSocket server = null;
    Socket sock = null;
    InputStream in = null;
    OutputStream out = null;

    try {
      server = new ServerSocket(port);
      while (true) {
        sock = server.accept();
        out = sock.getOutputStream();
        in = sock.getInputStream();
        byte[] response = createResponse(in);
        out.write(response);
        out.flush();
        in.close();
        out.close();
        sock.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (sock != null) {
        try {
          server.close();
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * Used in the "/random" endpoint
   */
  private final static HashMap<String, String> _images = new HashMap<>() {
    {
      put("streets", "https://iili.io/JV1pSV.jpg");
      put("bread", "https://iili.io/Jj9MWG.jpg");
    }
  };

  private Random random = new Random();

  /**
   * Reads in socket stream and generates a response
   * @param inStream HTTP input stream from socket
   * @return the byte encoded HTTP response
   */
  public byte[] createResponse(InputStream inStream) {

    byte[] response = null;
    BufferedReader in = null;

    try {

      // Read from socket's input stream. Must use an
      // InputStreamReader to bridge from streams to a reader
      in = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));

      // Get header and save the request from the GET line:
      // example GET format: GET /index.html HTTP/1.1

      String request = null;

      boolean done = false;
      while (!done) {
        String line = in.readLine();

        System.out.println("Received: " + line);

        // find end of header("\n\n")
        if (line == null || line.equals(""))
          done = true;
        // parse GET format ("GET <path> HTTP/1.1")
        else if (line.startsWith("GET")) {
          int firstSpace = line.indexOf(" ");
          int secondSpace = line.indexOf(" ", firstSpace + 1);

          // extract the request, basically everything after the GET up to HTTP/1.1
          request = line.substring(firstSpace + 2, secondSpace);
        }

      }
      System.out.println("FINISHED PARSING HEADER\n");

      // Generate an appropriate response to the user
      if (request == null) {
        response = "<html>Illegal request: no GET</html>".getBytes();
      } else {
        // create output buffer
        StringBuilder builder = new StringBuilder();
        // NOTE: output from buffer is at the end

        if (request.length() == 0) {
          // shows the default directory page

          // opens the root.html file
          String page = new String(readFileInBytes(new File("www/root.html")));
          // performs a template replacement in the page
          page = page.replace("${links}", buildFileList());

          // Generate response
          builder.append("HTTP/1.1 200 OK\n");
          builder.append("Content-Type: text/html; charset=utf-8\n");
          builder.append("\n");
          builder.append(page);

        } else if (request.equalsIgnoreCase("json")) {
          // shows the JSON of a random image and sets the header name for that image

          // pick a index from the map
          int index = random.nextInt(_images.size());

          // pull out the information
          String header = (String) _images.keySet().toArray()[index];
          String url = _images.get(header);

          // Generate response
          builder.append("HTTP/1.1 200 OK\n");
          builder.append("Content-Type: application/json; charset=utf-8\n");
          builder.append("\n");
          builder.append("{");
          builder.append("\"header\":\"").append(header).append("\",");
          builder.append("\"image\":\"").append(url).append("\"");
          builder.append("}");

        } else if (request.equalsIgnoreCase("random")) {
          // opens the random image page

          // open the index.html
          File file = new File("www/index.html");

          // Generate response
          builder.append("HTTP/1.1 200 OK\n");
          builder.append("Content-Type: text/html; charset=utf-8\n");
          builder.append("\n");
          builder.append(new String(readFileInBytes(file)));

        } else if (request.contains("file/")) {
          // tries to find the specified file and shows it or shows an error

          // take the path and clean it. try to open the file
          File file = new File(request.replace("file/", ""));

          // Generate response
          if (file.exists()) { // success
            builder.append("HTTP/1.1 200 OK\n");
            builder.append("Content-Type: text/html; charset=utf-8\n");
            builder.append("\n");
            builder.append("Would theoretically be a file but removed this part, you do not have to do anything with it for the assignment");
          } else { // failure
            builder.append("HTTP/1.1 404 Not Found\n");
            builder.append("Content-Type: text/html; charset=utf-8\n");
            builder.append("\n");
            builder.append("File not found: " + file);
          }
        } else if (request.contains("multiply?")) {
          // This multiplies two numbers, there is NO error handling, so when
          // wrong data is given this just crashes

          Map<String, String> query_pairs = new LinkedHashMap<String, String>();
          // extract path parameters
          String parameters = request.replace("multiply?", "");

          if (!parameters.equals("")) {
            query_pairs = splitQuery(request.replace("multiply?", ""));
          }

          System.out.println("query_pairs: " + query_pairs);


          // extract required fields from parameters
          Integer num1 = null; // Integer.parseInt(query_pairs.get("num1"));
          Integer num2 = null; // Integer.parseInt(query_pairs.get("num2"));

//          // do math
//          Integer result = num1 * num2;
//
//          // Generate response
//          builder.append("HTTP/1.1 200 OK\n");
//          builder.append("Content-Type: text/html; charset=utf-8\n");
//          builder.append("\n");
//          builder.append("Result is: " + result);

////          // Integer to store values of parameters
////          Integer number1 = null;
////          Integer number2 = null;
////          // Variable of type int to store whether query parameters are valid
////          int valid = 1;
////
////          try {
////            // extract required fields from parameters
////            number1 = Integer.parseInt(query_pairs.get("num1"));
////            System.out.println(number1);
////            number2 = Integer.parseInt(query_pairs.get("num2"));
////            System.out.println(number2);
////          } catch (NumberFormatException numberFormatException) {
////            builder.append("HTTP/1.1 406 Not Acceptable\n");
////            builder.append("Content-Type: text/html; charset=utf-8\n");
////            builder.append("\n");
////            builder.append("Error Code 406: Please enter integer values only.\n");
////            valid = 0;
////          } catch (IllegalArgumentException illegalArgumentException) {
////            builder.append("HTTP/1.1 400 Bad Request\n");
////            builder.append("Content-Type: text/html; charset=utf-8\n");
////            builder.append("\n");
////            builder.append("Error Code 400: Please enter two query parameters, e.g. num1=1&num2=2\n");
////            valid = 0;
////          }

          // TODO: Include error handling here with a correct error code and
          // a response that makes sense

          if (query_pairs.size() == 0 || !query_pairs.containsKey("num1") || !query_pairs.containsKey("num2")) {
            builder.append("HTTP/1.1 400 Bad Request\n");
            builder.append("Content-Type: text/html; charset=utf-8\n");
            builder.append("\n");
            builder.append("Error Code 400: Please enter two query parameters, e.g. num1=1&num2=2\n");
          } else {
            try {
              num1 = Integer.parseInt(query_pairs.get("num1"));
              num2 = Integer.parseInt(query_pairs.get("num2"));

              // do math
              Integer result = num1 * num2;

              // Generate response
              builder.append("HTTP/1.1 200 OK\n");
              builder.append("Content-Type: text/html; charset=utf-8\n");
              builder.append("\n");
              builder.append("Result is: " + result);
            } catch (NumberFormatException e) {
              builder.append("HTTP/1.1 406 Not Acceptable\n");
              builder.append("Content-Type: text/html; charset=utf-8\n");
              builder.append("\n");
              builder.append("Error Code 406: Please enter integer values only.\n");
            }
          }

        } else if (request.contains("github?")) {
          // pulls the query from the request and runs it with GitHub's REST API
          // check out https://docs.github.com/rest/reference/
          //
          // HINT: REST is organized by nesting topics. Figure out the biggest one first,
          //     then drill down to what you care about
          // "Owner's repo is named RepoName. Example: find RepoName's contributors" translates to
          //     "/repos/OWNERNAME/REPONAME/contributors"

          Map<String, String> query_pairs = new LinkedHashMap<String, String>();

          String parameters = request.replace("github?", "");

          if (!parameters.equals("")) {
            query_pairs = splitQuery(request.replace("github?", ""));
          }

          String[] queryParam = query_pairs.get("query").split("/");
          System.out.println("Query size: " + queryParam.length);

          String json = fetchURL("https://api.github.com/" + query_pairs.get("query"));
          System.out.println("JSON: " + json);
          System.out.println("JSON size: " + json.length());

//          builder.append("HTTP/1.1 200 OK\n");
//          builder.append("Content-Type: text/html; charset=utf-8\n");
//          builder.append("\n");
//          builder.append("Check the todos mentioned in the Java source file");
          // TODO: Parse the JSON returned by your fetch and create an appropriate
          // response based on what the assignment document asks for

          if (query_pairs.size() == 0) {
            builder.append("HTTP/1.1 400 Bad Request\n");
            builder.append("Content-Type: text/html; charset=utf-8\n");
            builder.append("\n");
            builder.append("Please enter query, e.g. query=users/OWNERNAME/repos\n");
          } else if (queryParam.length != 3) {
            builder.append("HTTP/1.1 400 Bad Request\n");
            builder.append("Content-Type: text/html; charset=utf-8\n");
            builder.append("\n");
            builder.append("Please enter query, e.g. query=users/OWNERNAME/repos\n");
          } else if (!queryParam[0].equals("users") || !queryParam[2].equals("repos")) {
            builder.append("HTTP/1.1 400 Bad Request\n");
            builder.append("Content-Type: text/html; charset=utf-8\n");
            builder.append("\n");
            builder.append("Please enter query, e.g. query=users/OWNERNAME/repos\n");
          } else if (json.length() < 1) {
            builder.append("HTTP/1.1 404 Not Found\n");
            builder.append("Content-Type: text/html; charset=utf-8\n");
            builder.append("\n");
            builder.append("Github could not be found. Please try again.\n");
          } else {
            JSONArray gitHubArray = new JSONArray(json);

            if (gitHubArray.length() == 0) {
              builder.append("HTTP/1.1 204 No Content\n");
              builder.append("Content-Type: text/html; charset=utf-8\n");
              builder.append("\n");
              builder.append("This github does not have public repositories.\n");
            } else {
              builder.append("HTTP/1.1 200 OK\n");
              builder.append("Content-Type: text/html; charset=utf-8\n");
              builder.append("\n");

              for (int i = 0; i < gitHubArray.length(); i++) {
                JSONObject newRepo = gitHubArray.getJSONObject(i);

                String repoName = newRepo.getString("full_name");
                int repoID = newRepo.getInt("id");
                String login = newRepo.getJSONObject("owner").getString("login");

                builder.append("Repository " + i + " - fullname: " + repoName + " id: " + repoID + " login: " + login + "\n");
                builder.append("\n");
              }
            }
          }
        } else if (request.contains("currentGrade?")) {
          // This uses the points earned on assignments, quizzes, and the exam in SER321 to
          // calculate the individuals overall grade in the class.

          Map<String, String> query_pairs = new LinkedHashMap<String, String>();
          // extract path parameters
          String parameters = request.replace("currentGrade?", "");

          if (!parameters.equals("")) {
            query_pairs = splitQuery(parameters);
          }

          System.out.println("query_pairs: " + query_pairs);

          // extract required fields from parameters
          Double assignment = null; // Integer.parseInt(query_pairs.get("assign"));
          Double quiz = null; // Integer.parseInt(query_pairs.get("quiz"));
          Double exam = null; // Integer.parseInt(query_pairs.get("exam"));

          Double assignTotal = 600.0;
          Double quizTotal = 100.0;
          Double examTotal = 300.0;

          // TODO: Include error handling here with a correct error code and
          // a response that makes sense

          if (query_pairs.size() == 0) {
            builder.append("HTTP/1.1 400 Bad Request\n");
            builder.append("Content-Type: text/html; charset=utf-8\n");
            builder.append("\n");
            builder.append("Please enter at least the assignment and quiz parameters, e.g. assign=540&quiz=85\n");
          } else if (!query_pairs.containsKey("assign")) {
            builder.append("HTTP/1.1 400 Bad Request\n");
            builder.append("Content-Type: text/html; charset=utf-8\n");
            builder.append("\n");
            builder.append("Please enter the assignment parameter, e.g. assign=540\n");
          } else if (!query_pairs.containsKey("quiz")) {
            builder.append("HTTP/1.1 400 Bad Request\n");
            builder.append("Content-Type: text/html; charset=utf-8\n");
            builder.append("\n");
            builder.append("Please enter the quiz parameter, e.g. quiz=85\n");
          } else {
              try {
                assignment = Double.parseDouble(query_pairs.get("assign"));
                quiz = Double.parseDouble(query_pairs.get("quiz"));

                if (!query_pairs.containsKey("exam")) {
                  exam = 0.0;
                } else {
                  exam = Double.parseDouble(query_pairs.get("exam"));
                }

                // do math
                Double grade = ((assignment / assignTotal * 60.0) + (quiz / quizTotal * 10.0) + (exam / examTotal * 30.0));

                String letterGrade = "";
                if (grade >= 97.0) {
                  letterGrade = "A+";
                } else if (grade >= 93.0) {
                  letterGrade = "A";
                } else if (grade >= 90.0) {
                  letterGrade = "A-";
                } else if (grade >= 87.0) {
                  letterGrade = "B+";
                } else if (grade >= 83.0) {
                  letterGrade = "B";
                } else if (grade >= 80.0) {
                  letterGrade = "B-";
                } else if (grade >= 77.0) {
                  letterGrade = "C+";
                } else if (grade >= 73.0) {
                  letterGrade = "C";
                } else if (grade >= 70.0) {
                  letterGrade = "C-";
                } else if (grade >= 67.0) {
                  letterGrade = "D+";
                } else if (grade >= 63.0) {
                  letterGrade = "D";
                } else if (grade >= 60.0) {
                  letterGrade = "D-";
                } else {
                  letterGrade = "F";
                }

                // Generate response
                builder.append("HTTP/1.1 200 OK\n");
                builder.append("Content-Type: text/html; charset=utf-8\n");
                builder.append("\n");
                builder.append("Calculation is: " + grade + "     CURRENT GRADE: " + letterGrade + "\n");
              } catch (NumberFormatException e) {
                builder.append("HTTP/1.1 406 Not Acceptable\n");
                builder.append("Content-Type: text/html; charset=utf-8\n");
                builder.append("\n");
                builder.append("Please enter number values only.\n");
              }
            }
        } else if (request.contains("cashier?")) {
          // This calculates the change that needs to be given to a customer based on the total and the amount paid
          // to the cashier.

          Map<String, String> query_pairs = new LinkedHashMap<String, String>();
          // extract path parameters
          String parameters = request.replace("cashier?", "");

          if (!parameters.equals("")) {
            query_pairs = splitQuery(request.replace("cashier?", ""));
          }

          System.out.println("query_pairs: " + query_pairs);

          // extract required fields from parameters
          Double price = null; // Integer.parseInt(query_pairs.get("num1"));
          Double paid = null; // Integer.parseInt(query_pairs.get("num2"));

          // TODO: Include error handling here with a correct error code and
          // a response that makes sense

          if (query_pairs.size() == 0 || !query_pairs.containsKey("price") || !query_pairs.containsKey("paid")) {
            builder.append("HTTP/1.1 400 Bad Request\n");
            builder.append("Content-Type: text/html; charset=utf-8\n");
            builder.append("\n");
            builder.append("Please enter the price and paid parameters, e.g. price=21.50&paid=22.00\n");
          } else if (!query_pairs.containsKey("price")) {
            builder.append("HTTP/1.1 400 Bad Request\n");
            builder.append("Content-Type: text/html; charset=utf-8\n");
            builder.append("\n");
            builder.append("Please enter the price parameter, e.g. price=23.23\n");
          } else if (!query_pairs.containsKey("paid")) {
            builder.append("HTTP/1.1 400 Bad Request\n");
            builder.append("Content-Type: text/html; charset=utf-8\n");
            builder.append("\n");
            builder.append("Please enter the paid parameter, e.g. paid=24.00\n");
          } else if (query_pairs.get("paid") < 0.0 || query_pairs.get("price") < 0.0) {
            builder.append("HTTP/1.1 400 Bad Request\n");
            builder.append("Content-Type: text/html; charset=utf-8\n");
            builder.append("\n");
            builder.append("Please enter values that are equal to or greater than 0.00\n");
          } else {
            try {
              price = Double.parseDouble(query_pairs.get("price"));
              paid = Double.parseDouble(query_pairs.get("paid"));

              // do math
              Double change = paid - price;

              if (change < 0) {
                // Generate response
                builder.append("HTTP/1.1 200 OK\n");
                builder.append("Content-Type: text/html; charset=utf-8\n");
                builder.append("\n");
                builder.append("The payment is not enough, please try again!\n");
              } else {
                Double bills = Math.abs(change);
                Double coins = change / bills;

                Double quarters = Math.abs(coins / 0.25);
                if (quarters > 0.0) {
                  coins = coins - (quarters * 0.25);
                }
                Double dimes = Math.abs(coins / 0.10);
                if (dimes > 0.0) {
                  coins = coins - (dimes * 0.10);
                }
                Double nickels = Math.abs(coins / 0.05);
                if (nickels > 0.0) {
                  coins = coins - (nickels * 0.05);
                }
                Double pennies = Math.abs(coins / 0.01);
                if (pennies > 0.0) {
                  coins = coins - (pennies * 0.01);
                }

                // Generate response
                builder.append("HTTP/1.1 200 OK\n");
                builder.append("Content-Type: text/html; charset=utf-8\n");
                builder.append("\n");
                builder.append("The change is: " + change + "Distribute coins:  Quarters - " + quarters + " Dimes - " +
                        dimes + " Nickels - " + nickels + " Pennies - " + pennies + "\n");
              }
            } catch (NumberFormatException e) {
              builder.append("HTTP/1.1 406 Not Acceptable\n");
              builder.append("Content-Type: text/html; charset=utf-8\n");
              builder.append("\n");
              builder.append("Please enter number values only.\n");
            }
          }

        } else {
          // if the request is not recognized at all

          builder.append("HTTP/1.1 400 Bad Request\n");
          builder.append("Content-Type: text/html; charset=utf-8\n");
          builder.append("\n");
          builder.append("I am not sure what you want me to do...");
        }

        // Output
        response = builder.toString().getBytes();
      }
    } catch (IOException e) {
      e.printStackTrace();
      response = ("<html>ERROR: " + e.getMessage() + "</html>").getBytes();
    }

    return response;
  }

  /**
   * Method to read in a query and split it up correctly
   * @param query parameters on path
   * @return Map of all parameters and their specific values
   * @throws UnsupportedEncodingException If the URLs aren't encoded with UTF-8
   */
  public static Map<String, String> splitQuery(String query) throws UnsupportedEncodingException {
    Map<String, String> query_pairs = new LinkedHashMap<String, String>();
    // "q=hello+world%2Fme&bob=5"
    String[] pairs = query.split("&");
    // ["q=hello+world%2Fme", "bob=5"]
    for (String pair : pairs) {
      int idx = pair.indexOf("=");
      query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"),
          URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
    }
    // {{"q", "hello world/me"}, {"bob","5"}}
    return query_pairs;
  }

  /**
   * Builds an HTML file list from the www directory
   * @return HTML string output of file list
   */
  public static String buildFileList() {
    ArrayList<String> filenames = new ArrayList<>();

    // Creating a File object for directory
    File directoryPath = new File("www/");
    filenames.addAll(Arrays.asList(directoryPath.list()));

    if (filenames.size() > 0) {
      StringBuilder builder = new StringBuilder();
      builder.append("<ul>\n");
      for (var filename : filenames) {
        builder.append("<li>" + filename + "</li>");
      }
      builder.append("</ul>\n");
      return builder.toString();
    } else {
      return "No files in directory";
    }
  }

  /**
   * Read bytes from a file and return them in the byte array. We read in blocks
   * of 512 bytes for efficiency.
   */
  public static byte[] readFileInBytes(File f) throws IOException {

    FileInputStream file = new FileInputStream(f);
    ByteArrayOutputStream data = new ByteArrayOutputStream(file.available());

    byte buffer[] = new byte[512];
    int numRead = file.read(buffer);
    while (numRead > 0) {
      data.write(buffer, 0, numRead);
      numRead = file.read(buffer);
    }
    file.close();

    byte[] result = data.toByteArray();
    data.close();

    return result;
  }

  /**
   *
   * a method to make a web request. Note that this method will block execution
   * for up to 20 seconds while the request is being satisfied. Better to use a
   * non-blocking request.
   * 
   * @param aUrl the String indicating the query url for the OMDb api search
   * @return the String result of the http request.
   *
   **/
  public String fetchURL(String aUrl) {
    StringBuilder sb = new StringBuilder();
    URLConnection conn = null;
    InputStreamReader in = null;
    try {
      URL url = new URL(aUrl);
      conn = url.openConnection();
      if (conn != null)
        conn.setReadTimeout(20 * 1000); // timeout in 20 seconds
      if (conn != null && conn.getInputStream() != null) {
        in = new InputStreamReader(conn.getInputStream(), Charset.defaultCharset());
        BufferedReader br = new BufferedReader(in);
        if (br != null) {
          int ch;
          // read the next character until end of reader
          while ((ch = br.read()) != -1) {
            sb.append((char) ch);
          }
          br.close();
        }
      }
      in.close();
    } catch (Exception ex) {
      System.out.println("Exception in url request:" + ex.getMessage());
    }
    return sb.toString();
  }
}
