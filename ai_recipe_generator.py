import os
import json
import argparse # Add argparse
from dataclasses import dataclass, field
from typing import List, Optional, Dict, Any
import google.generativeai as genai
from dotenv import load_dotenv
import sys # Import sys for stderr output

# Load environment variables (ensure you have a .env file with GOOGLE_API_KEY)
load_dotenv()

# Configure the Gemini API key
GOOGLE_API_KEY = os.getenv("GOOGLE_API_KEY")
if not GOOGLE_API_KEY:
    # Instead of raising error immediately, print error and exit if run as script
    # This allows Java side to potentially handle missing key gracefully if needed
    print("Error: GOOGLE_API_KEY not found in environment variables.", file=sys.stderr)
    # sys.exit(1) # Consider if exiting here is better than letting genai fail
    pass # Let genai.configure fail later if key is truly missing

genai.configure(api_key=GOOGLE_API_KEY)

@dataclass
class Recipe:
    """Represents a generated recipe."""
    title: str
    ingredients: List[str]  # Full list including quantities, e.g., ["1 cup flour", "2 eggs"]
    instructions: List[str] # Step-by-step instructions
    prep_time_minutes: Optional[int] = None
    cook_time_minutes: Optional[int] = None
    servings: Optional[int] = None
    tags: List[str] = field(default_factory=list) # e.g., ["vegetarian", "quick", "italian"]
    thumbnail_url: Optional[str] = None # Optional image URL

    def to_dict(self) -> Dict[str, Any]:
        """Converts the Recipe object to a dictionary."""
        return {
            "title": self.title,
            "ingredients": self.ingredients,
            "instructions": self.instructions,
            "prep_time_minutes": self.prep_time_minutes,
            "cook_time_minutes": self.cook_time_minutes,
            "servings": self.servings,
            "tags": self.tags,
            "thumbnail_url": self.thumbnail_url,
        }


def generate_recipe(
    ingredients: Optional[List[str]] = None,
    cuisine: Optional[str] = None,
    dietary_restrictions: Optional[List[str]] = None,
    excluded_ingredients: Optional[List[str]] = None,
    meal_type: Optional[str] = None,
    custom_prompt: Optional[str] = None
) -> Optional[Recipe]:
    """
    Generates a recipe using the Google Gemini API based on user inputs.

    Args:
        ingredients: List of available ingredients.
        cuisine: Desired cuisine style (e.g., "Italian", "Mexican").
        dietary_restrictions: List of dietary restrictions (e.g., ["vegetarian", "gluten-free"]).
        excluded_ingredients: List of ingredients to exclude.
        meal_type: Type of meal (e.g., "Dinner", "Breakfast", "Snack").
        custom_prompt: Any additional specific requests from the user.

    Returns:
        A Recipe object if generation is successful, otherwise None.
    """
    prompt_parts = [
        "You are a helpful recipe assistant. Generate a detailed recipe based on the following requirements.",
        "Please provide the recipe in the following JSON format ONLY, with no introductory text or explanations before or after the JSON object:",
        """
{
  "title": "Recipe Title",
  "ingredients": ["quantity unit ingredient", "...", ...],
  "instructions": ["Step 1...", "Step 2...", ...],
  "prep_time_minutes": integer or null,
  "cook_time_minutes": integer or null,
  "servings": integer or null,
  "tags": ["tag1", "tag2", ...] // e.g., cuisine, meal type, diet tags, key ingredients,
  "thumbnail_url": "URL string or null" // Add this field - find a relevant, publicly accessible image URL if possible
}
""",
        "Requirements:"
    ]

    if ingredients:
        prompt_parts.append(f"- Use primarily these ingredients: {', '.join(ingredients)}")
    if cuisine:
        prompt_parts.append(f"- Cuisine Style: {cuisine}")
    if dietary_restrictions:
        prompt_parts.append(f"- Dietary Restrictions: {', '.join(dietary_restrictions)}")
    if excluded_ingredients:
        prompt_parts.append(f"- Exclude these ingredients: {', '.join(excluded_ingredients)}")
    if meal_type:
        prompt_parts.append(f"- Meal Type: {meal_type}")
    if custom_prompt:
        prompt_parts.append(f"- Additional Notes: {custom_prompt}")
    if not any([ingredients, cuisine, dietary_restrictions, excluded_ingredients, meal_type, custom_prompt]):
        prompt_parts.append("- Generate any interesting recipe.") # Default if no specific input

    final_prompt = "\\n".join(prompt_parts) # Use escaped newline for join

    try:
        # Initialize the generative model
        # Use a model optimized for JSON output if available, check Gemini docs
        # For now, using a standard model like 'gemini-pro'
        model = genai.GenerativeModel('gemini-1.5-flash') # Or 'gemini-pro' or other suitable model

        # Set generation config to potentially improve JSON output
        # generation_config = genai.types.GenerationConfig(
        #     response_mime_type="application/json" # Check if supported by the model version
        # )
        # response = model.generate_content(final_prompt, generation_config=generation_config)

        # Standard text generation call - we will parse the JSON from the text
        response = model.generate_content(final_prompt)

        # --- Response Parsing ---
        raw_text = response.text
        # --- Ensure this print goes to stderr --- 
        print(f"Raw AI Response (to stderr):\n{repr(raw_text)}", file=sys.stderr)

        # --- Extract JSON --- 
        # Find the first '{' and the last '}' to extract the JSON block
        json_start = raw_text.find('{')
        json_end = raw_text.rfind('}')
        
        json_string = None
        if json_start != -1 and json_end != -1 and json_end > json_start:
            json_string = raw_text[json_start : json_end + 1]
            # Basic validation: Does it look like JSON?
            if not (json_string.strip().startswith('{') and json_string.strip().endswith('}')):
                json_string = None # Reject if simple validation fails
        
        if json_string is None:
            print(f"Error: Could not reliably extract JSON object from AI response (start={json_start}, end={json_end}).", file=sys.stderr)
            # Print the attempt for debugging
            print(f"Raw text was:\n{raw_text}", file=sys.stderr)
            return None

        # --- Parse JSON String ---
        try:
            recipe_data = json.loads(json_string)
        except json.JSONDecodeError as e:
            print(f"Error: Failed to decode JSON from AI response: {e}", file=sys.stderr)
            # Use repr() for safer printing here too
            print(f"Extracted JSON string attempt:\\n{repr(json_string)}", file=sys.stderr)
            return None

        # Validate essential fields
        if not recipe_data.get('title') or not recipe_data.get('ingredients') or not recipe_data.get('instructions'):
             print("Error: AI response missing essential fields (title, ingredients, instructions).")
             print(f"Received data: {recipe_data}")
             return None # Correctly indented

        # Create Recipe object
        recipe = Recipe(
            title=recipe_data['title'],
            ingredients=recipe_data['ingredients'],
            instructions=recipe_data['instructions'],
            prep_time_minutes=recipe_data.get('prep_time_minutes'),
            cook_time_minutes=recipe_data.get('cook_time_minutes'),
            servings=recipe_data.get('servings'),
            tags=recipe_data.get('tags', []),
            # --- Get thumbnail_url from AI response --- 
            thumbnail_url=recipe_data.get('thumbnail_url') # Can be None/null
        )
        return recipe # Correctly indented

    except Exception as e:
        # Catch potential API errors, configuration issues, etc.
        # Print errors to stderr so Java can capture them
        print(f"An error occurred during recipe generation: {e}", file=sys.stderr)
        return None # Correctly indented

# Function to run generation and print JSON result to stdout
def run_generation_and_print(args):
    generated_recipe_object = generate_recipe(
        ingredients=args.ingredients,
        cuisine=args.cuisine,
        dietary_restrictions=args.dietary_restrictions,
        excluded_ingredients=args.excluded_ingredients,
        meal_type=args.meal_type,
        custom_prompt=args.custom_prompt
    )

    if generated_recipe_object:
        # Print the JSON output to stdout for Java process to read
        # THIS SHOULD BE THE ONLY PRINT TO STDOUT ON SUCCESS
        try:
            print(json.dumps(generated_recipe_object.to_dict()))
        except Exception as e:
             print(f"Error: Failed to serialize final recipe object to JSON: {e}", file=sys.stderr)
             sys.exit(1) # Exit with error code if serialization fails
    else:
        # Error messages during generation are printed to stderr inside generate_recipe
        # Exit with error code if generation fails
        sys.exit(1)

# Argument parsing block
if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Generate a recipe using AI based on inputs.')
    parser.add_argument('--ingredients', nargs='*', help='List of available ingredients')
    parser.add_argument('--cuisine', help='Desired cuisine style')
    parser.add_argument('--dietary-restrictions', nargs='*', help='List of dietary restrictions')
    parser.add_argument('--excluded-ingredients', nargs='*', help='List of ingredients to exclude')
    parser.add_argument('--meal-type', help='Type of meal (e.g., Dinner, Breakfast)')
    parser.add_argument('--custom-prompt', help='Any additional specific requests')

    args = parser.parse_args()

    # Make sure API key is configured before running generation
    if not GOOGLE_API_KEY:
         print("Error: GOOGLE_API_KEY must be configured in environment variables to run generation.", file=sys.stderr)
         sys.exit(1)

    run_generation_and_print(args)

# ----- Remove or comment out the old example usage block -----
# if __name__ == '__main__':
#     print("Testing AI Recipe Generation...")
#     # Make sure you have a .env file in the project root with your GOOGLE_API_KEY
#     test_recipe = generate_recipe(
#         ingredients=["chicken breast", "broccoli", "soy sauce", "rice"],
#         meal_type="Dinner",
#         custom_prompt="Make it relatively quick and healthy."
#     )
#
#     if test_recipe:
#         # Corrected print statement
#         print("\\nGenerated Recipe:")
#         print(f"Title: {test_recipe.title}")
#         # ... rest of old print statements ...
#     else:
#         # Corrected print statement
#         print("\\nFailed to generate recipe.") 