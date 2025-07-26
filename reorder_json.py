import json
from pathlib import Path

FIELD_ORDER = [
    "type", "category", "group", "blueprint", "requires_blueprint",
    "tier", "hammering", "has_quality", "minimum_quality", "needs_minigame",
    "has_polishing", "pattern", "key", "result", "result_failed", "show_notification"
]


def reorder(data: dict) -> dict:
    reordered = {k: data[k] for k in FIELD_ORDER if k in data}
    # Add any remaining keys at the end
    for k in data:
        if k not in reordered:
            reordered[k] = data[k]
    return reordered


def process_all():
    recipe_dir = Path("src/generated/resources/data")
    print(f"Scanning directory: {recipe_dir.resolve()}")
    file_count = 0
    skipped_count = 0

    for path in recipe_dir.rglob("*.json"):
        with path.open(encoding="utf-8") as f:
            try:
                data = json.load(f)
            except json.JSONDecodeError as e:
                print(f"Error reading {path}: {e}")
                continue

        # Only process if type is "overgeared:forging"
        if data.get("type") != "overgeared:forging":
            print(f"Skipping {path} (type is not 'overgeared:forging')")
            skipped_count += 1
            continue

        file_count += 1
        print(f"\nProcessing file: {path}")
        print("Original order:", list(data.keys()))

        reordered_data = reorder(data)
        print("Reordered keys:", list(reordered_data.keys()))

        with path.open("w", encoding="utf-8") as f:
            json.dump(reordered_data, f, indent=2)
        print("File saved with reordered keys.")

    print(f"\nFinished. Processed {file_count} file(s), skipped {skipped_count}.")


if __name__ == "__main__":
    process_all()
