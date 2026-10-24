Identify all visible cooking ingredients in the image.
Only identify food ingredients and ignore all other objects.

Return each ingredient with the following fields:
- name: ingredient name in Vietnamese.
- nameEn: ingredient name in English.
- confidence: confidence score between 0 and 1.
- quantityHint: estimated quantity in Vietnamese, or null if unclear.

Return only a valid JSON array without Markdown or explanations.
If no ingredients are visible, return an empty array.