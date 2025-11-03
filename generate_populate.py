import os
import csv

def format_sql_value(value):
    """
    Formats a value for insertion into an SQL statement.
    - Empty strings are treated as NULL.
    - Numeric values are left as-is.
    - String values are wrapped in single quotes, and internal single
      quotes are escaped (e.g., 'O'Malley' -> '''O''Malley''').
    """
    if value is None:
        return "NULL"
    
    value_str = str(value).strip()
    
    if not value_str:
        return "NULL"

    try:
        float(value_str)
        return value_str
    except ValueError:
        escaped_value = value_str.replace("'", "''")
        return f"'{escaped_value}'"

def generate_populate_script():
    """
    Reads CSV files from the 'CSV_files' directory and generates
    a 'Populate.txt' SQL script for populating an SQLite database.
    """
    csv_dir = 'CSV_files'
    output_file = 'Populate.txt'

    csv_files = [
        'address.csv',
        'batteries.csv',
        'battery_type.csv',
        'drone_model.csv',
        'drones.csv',
        'equip_model.csv',
        'equipment.csv',
        'mechanics.csv',
        'members.csv',
        'order_price.csv',
        'orders.csv',
        'rent.csv',
        'rentals.csv',
        'repair.csv',
        'reviews.csv',
        'transports.csv',
        'warehouse.csv'
    ]

    try:
        with open(output_file, 'w', encoding='utf-8') as f_out:

            f_out.write("BEGIN TRANSACTION;\n\n")

            for csv_file in csv_files:
                table_name = os.path.splitext(csv_file)[0]
                csv_path = os.path.join(csv_dir, csv_file)

                if not os.path.exists(csv_path):
                    print(f"Warning: '{csv_path}' not found. Skipping.")
                    continue

                print(f"Processing {csv_file} -> {table_name} table...")
                
                try:
                    with open(csv_path, 'r', encoding='utf-8-sig') as f_in:

                        reader = csv.reader(f_in)

                        try:
                            header = next(reader)
                        except StopIteration:
                            print(f"Warning: '{csv_file}' is empty. Skipping.")
                            continue

                        column_names = ', '.join([f'"{col}"' for col in header])

                        f_out.write(f"-- Populating {table_name}\n")

                        for row in reader:
                            if not any(row): 
                                continue

                            if len(row) != len(header):
                                print(f"Warning: Skipping row in {csv_file} due to column mismatch.")
                                continue
                                
                            formatted_values = ', '.join([format_sql_value(val) for val in row])
                            
                            sql_statement = f"INSERT INTO {table_name} ({column_names}) VALUES ({formatted_values});\n"
                            f_out.write(sql_statement)
                        
                        f_out.write("\n")

                except Exception as e:
                    print(f"Error processing file {csv_file}: {e}")

            f_out.write("COMMIT;\n")
            
        print(f"\nSuccessfully generated '{output_file}'!")
        print("You can now run this SQL script on your SQLite database.")

    except IOError as e:
        print(f"Error writing to output file {output_file}: {e}")
    except Exception as e:
        print(f"An unexpected error occurred: {e}")

if __name__ == "__main__":
    generate_populate_script()
