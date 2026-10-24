# resources/prompts/recipe-ideas.txt
Ingredients available in the user's pantry: {ingredients}
Preferred cuisine: {cuisine}
Maximum cooking time: {maxMinutes} minutes

Suggest up to {limit} dishes that can be made mainly from the ingredients above.

Rules:
- Write all text values (name, description, ingredient names) in Vietnamese.
- Keep JSON keys exactly as specified, in English.
- usedIngredients: ingredients from the pantry list that the dish uses.
- missingIngredients: extra ingredients the user would still need to buy.
- estimatedMinutes: integer, must not exceed the maximum cooking time if one is given.
- confidence: number from 0 to 1 indicating how well the dish matches the available ingredients.
- Only suggest dishes. Do not give medical or nutrition advice.

Return a JSON array only, no markdown or extra text. Each element has these keys:
name, description, estimatedMinutes, usedIngredients, missingIngredients, confidence.