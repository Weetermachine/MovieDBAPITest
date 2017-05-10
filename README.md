# MovieDBAPITest

In order to run the tests, please first download the repository, and open up "MovieDbAPITest.java" file, and put your API token into the
APITOKEN final variable at the very top of the file. Please then save the file, and open Terminal and navigate to the "src" directory 
of the project. 

From there, first run the following command to compile the java Test code:
javac -cp $(echo ../libs/*.jar | tr ' ' ':'):Weeter/: Weeter/*.java

After it completes, please run the following command to run the tests:
java -cp $(echo ../libs/*.jar | tr ' ' ':'):Weeter/: org.junit.runner.JUnitCore Weeter.MovieDbAPITest

If you have any issues or questions, please contact me at christopher.weeter@gmail.com
