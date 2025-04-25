# Set up your local [environment]()
## Recommended Software
- MariaDB
- Jetbrains Intellij
- Github Desktop
- Jetbrains DataGrip (less needed, but helpful if you don't know SQL well)

## Instructions
1. Download and install MariaDB
   2. Download MariaDB for your operating System
   3. Run the installer
4. Create a local database
   5. If you are NOT using DataGrip:
      6. If you are using windows
         7. open command prompt
         8. CD to mariadb bin folder (something like C:\Program Files\MariaDB 11.6\bin)
         9. run mysql -u root -p and enter the root password you chose
         10. run command "create schema pantrypilot" (you can name the schema what you want, just remember it for later)
         11. If you don't want to use the root user, you can also create another user
      12. If you are using macOS
          13. 
   14. If you are using DataGrip
       15. open datagrip and click the plus button, add a new datasource for mariadb
       16. Enter information
           17. Host: localhost
           18. Authentication: user & password
           19. User: root
           20. Password: (the password you set)
           21. Database: leave blank for now
       22. If it asks you to download a driver, download it
       23. Test connection, if it works hit OK
       24. Create the Schema
           25. Right-click on the new data source on the left side of the screen (probably called localhost)
           26. Go to New > Query Console
           27. type "CREATE SCHEMA pantrypilot" (or whatever you want to call your database, but make sure you remember it)
           28. Run the SQL statement with the green play button
28. Clone the repo
    29. Sign in to GitHub desktop
    30. Go to repositories > Add > Clone a repo > food-project
        31. This will clone the main branch into user/documents/GitHub/food-project by default
32. Setup Intellij
    33. Open Intellij and click "open" and navigate to where the repo is stored
    34. Once the project is open, go to File > Project Structure (or ctrl + alt + shift + s on windows)
        35. Set the SDK to java 1.8, download one if you need to by clicking Add SDK > Download JDK, select version: 1.8 and Vendor: amazon Corretto
        36. Set the language level to SDK Default (or 8, but if anything else is chosen your code may not work)
        36. Then navigate to "modules" on the left, then ensure you are on the "sources" tab
            37. Navigate to src/main/java, select the java folder then click the "Sources" button at the top
            38. Select the resources folder just below the java folder (src/main/resources) and click the button at the top for "Resources"
            39. Navigate to src/test/java, select the java folder and click the button "Tests"
        40. After marking all 3 folders, click OK
41. Setup config
    42. Make a copy of /src/main/resources/config.properties.template and remove the .template part
        43. This is your version of the config, this code contains password and data that should NEVER be put in Git
        44. for that reason, it is excluded and will not show up in your commits.
    45. Now open your new config.properties file
        46. change server.port to 80
        47. If you did a custom schema name, replace pantrypilot in the database.url with your schema name
        48. Only change username if you made a custom user
        49. update the password with the root password you made when installing MariaDB, or your computer password
        50. Save the file
51. You should now be ready to run a local instance of PantryPilot
    52. In intellij, I suggest you run it with maven, but you can also run the main method directly
    53. To run the code with maven, go to the maven tab on the right (the "m" button)
        54. then go to PantryPilot > Lifecycle > double click package
            55. This will build and test your code
        56. Now that you have ran package, you have a ready to run JAR file
        57. In the Intellij file explorer, navigate to /target and right click "PantryPilot-1.0-SNAPSHOT.jar", then select "Run..."
            58. do not do either of the other jar files such as original or shaded, as these aren't the fully built JAR files
    59. Watch the console for errors, if no errors you should be good to open a browser and go to http://localhost and you should see the website



## Developing
### We have 2 CI/CD enabled branches
- dev is our dev environment found at https://dev.pantrypilot.pro
- main is the prod environment found at https://pantrypilot.pro
### Any commits or pull requests to either of these branches will instantly be deployed on the server
- for this reason, Jacob is the only one who should merge a PR or commit directly to either of these branches
### To write your own code
- Create a new branch based on dev (probably)
- name the branch what work you are doing, for example "add-account-functionality"
- You are free to commit and work on this branch on your local environment
- Once your code works well locally, in Github you can make a pull request from your branch to dev
  - In this pull request, describe the changes you made
  - do NOT merge this pull request, but you are free to approve it
  - Notify the group chat that you made a new pull request, so we can take a look at it
  - Once the pull request looks good, Jacob will approve and merge it into dev, where the code will instantly be deployed