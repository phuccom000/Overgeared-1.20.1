import os
import json

# Set your folder path here
RECIPES_DIR = "D:/Minecraft Mods/Overgeared New instance/src/main/resources/data/slavicarmory/recipes"

MOD_ID = "slavicarmory"

def wrap_with_forge_conditional(recipe_json):
    return {
        "type": "forge:conditional",
        "recipes": [
            {
                "conditions": [
                    {
                        "type": "forge:mod_loaded",
                        "modid": MOD_ID
                    }
                ],
                "recipe": recipe_json
            }
        ]
    }

def process_recipe_file(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        try:
            original = json.load(f)
        except json.JSONDecodeError:
            print(f"Skipping invalid JSON: {file_path}")
            return

    # Skip if already conditional
    if original.get("type") == "forge:conditional":
        print(f"Already conditional: {file_path}")
        return

    # Wrap and overwrite
    wrapped = wrap_with_forge_conditional(original)
    with open(file_path, 'w', encoding='utf-8') as f:
        json.dump(wrapped, f, indent=2)
        print(f"Wrapped: {file_path}")

def process_folder(folder):
    for root, _, files in os.walk(folder):
        for file in files:
            if file.endswith(".json"):
                process_recipe_file(os.path.join(root, file))

# Run it
process_folder(RECIPES_DIR)
