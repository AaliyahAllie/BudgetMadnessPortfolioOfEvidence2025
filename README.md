**Budget Madness**

This Read Me file contains the manual and explanation for how our application works.

**LINKS FOR USE:**

**GITHUB:**
https://github.com/AaliyahAllie/BudgetMadnessPortfolioOfEvidence2025.git

**YOUTUBE:**
https://youtu.be/3lX5PyEfqqs?si=5GQZn-tubbFp2XfY


**Introduction**
This repository contains the code for Budget Madness, developed as part of our Part 3 (PORTFOLIO OF EVIDENCE) assignment in the Open-Source Coding course.
The app is designed to help users effectively manage their finances by tracking expenses, monitoring income, and planning for future financial goals.

**Feedback from Lecturer**
In our previous part we were told to make our application colour darker, as well as adding missing features,
like category totals (which we have implemented in this part and is displayed in the YouTube video).

**Contributors**
•	Aaliyah Allie
•	Amaan Williams
•	Aadam Naidoo
•	Kenneth Michael Remmitz
•	Moghammad Saadiq Jaatiem

**Programming Language**
This application is built using Kotlin, leveraging our knowledge of the language for Android development.

**Colour Scheme**
The app utilizes a colour scheme of dark green, white, and black for a clean, modern look.

**Database Storage**
For this part of the application, we are using Firebase Realtime Database to store data online so it can be accessed at 
any time even if the user uninstalls the application from their device. 

**New Features of Our Own**
1.	In App Notifications, when user has upcoming payments for the next 5 days.
2.	Alerts for creating budget limits per category.
3.	Payment reminder screen to set notification.
4.	In App Notification, when a user spends over R6000 for a selected time.

**App Functionality**
1. App Launcher Screen
•	Opens with a greeting, app logo, and options to either register or log in.
2. Register Screen
•	Allows users to create a profile by providing their first name, last name, username, email, phone number, and password. All details are saved to the database.
3. Login Screen
•	Allows users to log in based on their username and password, which are verified against the stored data in the database.
4. Get Started Screen
•	Displays a singular logo and a button that navigates to the home screen. It also will display a feature we created of our own not under the assignment criteria. That notifies a user if there are any upcoming payments over the next few days.
5. Home Screen
•	Displays user information:
•	Recently added expenses (highlighted in an orange block)
•	Recently added income (highlighted in a green block)
•	Balance (displayed in a black block)
•	A feature to generate a graph, that displays how much a user has spent, if they are over the amount for the selected budget period and how much they can still spend for the selected budget period. Chart is displayed with a pie chart.
•	Bottom navigation for navigating to the Income screen, Home screen, Expenses screen, and Menu.
6. Profile Screen
•	Allows users to update their details and add a profile image.
7. Add Expenses Screen
•	Users can add their expenses by providing the following details:
•	Expense name
•	Amount
•	Date
•	Category
•	Payment method (Cash or Card)
•	Receipt image (optional)
8. Income Screen
•	Allows users to add their income, specifying the payment method (Cash or Card).
9. Balance Screen
•	Displays the total balance, along with the history of income and expense transactions.
10. View Expenses Screen
•	Shows the history of expenses within a selected date range.
•	If user exceeds the limit of R6000 for a range they receive a notification.
11. Budget Screen
•	Allows users to set a maximum and minimum budget for each month. This feature will later display as a graph on the home screen in Part 3 of the project.
12. Bottom Nav
•	Bottom navigation for navigating to the Income screen, Home screen, Expenses screen, and Menu.
13.Menu
•	Navigates to all pages mentioned above
14.View Categories
•	Always users to see total amount spent of each category (implemented because we were missing it in the previous submission).
•	Displays a pie chart to display how much each category takes up for the selected range.
15.Payments Due Soon
•	Gives user the ability to enter any payments that are due soon.
•	This is used to display a message when a user logs in.
16. View Budget 
•	Displays the budget for a user monthly.
•	Uses a reward system

**Gamification:**
The application utilizes gamification rules by implementing, min and max for budgeting, displays a different cat image for different goals reached.

**Animation:**
When an income is added there is a cat that loads until the income is saved.

**Visual Graphs:**
Utilizes 2 graphs, graphs that visually represent data.
1.	Home screen displays budgeting progress against budget max and min and spending price.
2.	Categories, total amount for each category displayed through a pie chart.
3.	
We hope this app provides valuable financial management tools for users!



