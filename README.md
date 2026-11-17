# PantryPal: smart kitchen management and food waste reduction
   > Know what you have. Discover what you can cook. Waste less.
<img width="938" height="497" alt="image" src="https://github.com/user-attachments/assets/44a455d0-7388-4a55-a078-912384036cf6" />


PantryPal is a full-stack web application with a **Java Spring Boot + Spring Data JPA + Spring Security** backend with **MySQL** (or the built-in H2 database for zero-setup local runs) and an **HTML5 / CSS3 / vanilla JavaScript** frontend. The frontend and backend talk only through REST APIs.
It's a little kitchen companion that keeps track of what's in your cupboards, tells you what you can cook right now, nudges you before food goes off, and builds a shopping list that skips everything you already own.

Live Demo PantryPal Link: https://pantrypal-eb2d.onrender.com/

<img width="890" height="494" alt="image" src="https://github.com/user-attachments/assets/e6080cac-159e-4792-a1b9-c4ffea6abddb" />



# Key Features

- Digital Pantry: Add, edit and delete ingredients while tracking quantities, units and expiry dates. Supports up to 200 items per user.
- Smart Recipe Recommendations: Suggests and ranks recipes based on available ingredients, quantities and match percentages.
- Ingredient Substitutions: Recommends alternatives when ingredients are missing, with suitable quantities and unit conversions.
- Expiry Alerts: Automatically checks ingredients every hour and highlights items that need to be used soon.
- Meal Planner: Organises breakfast, lunch and dinner through a weekly calendar.
- Shopping Lists: Generates shopping lists from planned meals while subtracting ingredients already available in the pantry.
- Dietary Preferences: Supports Vegan, Vegetarian, Keto and General diets, along with allergy restrictions.
- Interactive Design: Includes a cute chef mascot, pixel-art icons, food illustrations and playful animations.

# Recipe Recommendation System

The recommendation engine checks the ingredients and quantities available in the user's pantry and calculates a match percentage for each recipe.

For example, if a recipe requires five ingredients and the user has four, the match is 80%.
Match Percentage: Total ingredient points ÷ Required ingredients × 100

Partial quantities receive proportional scores, while missing ingredients receive zero points.

We implemented the Strategy Design Pattern to support five recommendation strategies:

   Smart Mix- Balances all recommendation factors.
   Maximum Ingredient Match- Prioritises available ingredients.
   Expiring Ingredients First- Prioritises ingredients close to expiry.
   Dietary Preference- Prioritises dietary preferences.
   Minimum Missing Ingredients- Reduces additional purchases.

The system also calculates a waste-risk score from 0 to 100 based on expiry dates and ingredient quantities.

--------------------------------------------------------------------------------------------------------------------------------------------------------------
# Requirements
Apache NetBeans(Where had Maven)
PantryPal uses H2 by default, so no separate database installation is required.

# Demo Accounts

Email                        Password

demo@pantrypal.app           Demo1234

student@pantrypal.app        Student123


# Technologies Used

Component and Technology
   - Backend: Java 17, Spring Boot 3
   - Backend Frameworks: Spring Data JPA, Spring Security
   - Database: H2 / MySQL 8
   - Frontend: HTML5, CSS3, Vanilla JavaScript
   - Communication: REST API (JSON)
   - Build Tool: Maven
   - Deployment: Render

<img width="935" height="498" alt="image" src="https://github.com/user-attachments/assets/caf45b00-77e1-4e5f-8732-8595293834e0" />

<img width="935" height="492" alt="image" src="https://github.com/user-attachments/assets/378e6528-24b6-4568-a07d-3b696a7e3507" />

<img width="934" height="499" alt="image" src="https://github.com/user-attachments/assets/f9fbd5c4-d571-4e5f-98ad-79426ba63743" />

<img width="936" height="496" alt="image" src="https://github.com/user-attachments/assets/561a02e9-ba8e-4f41-ba81-18197175a866" />

<img width="938" height="497" alt="image" src="https://github.com/user-attachments/assets/7210f4bb-a84b-4b82-8f03-cc2ca3aad359" />
