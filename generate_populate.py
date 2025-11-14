import os
import csv
# THIS IS JUST A HELPER SCRIPT AND IT DOES NOT COMPLETLY HANDLE ALL CASES JUST LAYS FOUNDATION FOR POPULATE
SCHEMA_DEFINITION = {
    'warehouses': { # Matches 'warehouses.csv'
        'city': 'string',
        'address': 'string',
        'phone': 'string',
        'mName': 'string',
        'storeCap': 'numeric',
        'droneCap': 'numeric'
    },
    'address': { # Matches 'address.csv'
        'address': 'string',
        'userDist': 'numeric'
    },
    'members': { # Matches 'members.csv'
        'fname': 'string',
        'lname': 'string',
        'address': 'string',
        'phone': 'string',
        'email': 'string',
        'startDate': 'string',
        'userID': 'string'
    },
    'drone_model': { # Matches 'drone_model.csv'
        'weightCap': 'numeric',
        'year': 'numeric',
        'model': 'string',
        'maxSpeed': 'numeric',
        'maxDist': 'numeric',
        'manufacturer': 'string'
    },
    'battery_type': { # Matches 'battery_type.csv'
        'type': 'string',
        'voltage': 'numeric'
    },
    'batteries': { # Matches 'batteries.csv'
        'productID': 'string',
        'storageAMT': 'numeric',
        'chargedStatus': 'string',
        'type': 'string'
    },
    'drones': { # Matches 'drones.csv'
        'name': 'string',
        'model': 'string',
        'serialNum': 'string',
        'status': 'string',
        'location': 'string',
        'year': 'numeric',
        'WarehouseAddress': 'string',
        'BatteryID': 'string'
    },
    'order_price': { # Matches 'order_price.csv'
        'elementType': 'string',
        'quantity': 'numeric',
        'value': 'numeric'
    },
    'orders': { # Matches 'orders.csv'
        'orderNumber': 'numeric',
        'elementType': 'string',
        'Quantity': 'numeric',
        'estArrDate': 'string',
        'actArrDate': 'string',
        'userID': 'string'
    },
    'equip_model': { # Matches 'equip_model.csv'
        'type': 'string',
        'model': 'string',
        'year': 'numeric',
        'weight': 'numeric',
        'dimensions': 'string',
        'manufacturer': 'string'
    },
    'equipment': { # Matches 'equipment.csv'
        'description': 'string',
        'type': 'string',
        'model': 'string',
        'year': 'numeric',
        'serialNum': 'string',
        'status': 'string',
        'location': 'string',
        'warExp': 'string',
        'warehouseAddress': 'string',
        'orderNum': 'numeric',
        'renterID': 'string'
    },
    'rentals': { # Matches 'rentals.csv'
        'serialNum': 'string',
        'userID': 'string',
        'checkOutID': 'string',
        'dueDate': 'string',
        'rentalFees': 'numeric',
        'Returns': 'string'
    },
    'reviews': { # Matches 'reviews.csv'
        'serialNum': 'string',
        'userID': 'string',
        'comments': 'string',
        'ratings': 'numeric'
    },
    'mechanics': { # Matches 'mechanics.csv'
        'ssn': 'string',
        'specialty': 'string',
        'email': 'string',
        'phoneNum': 'string',
        'warehouseNum': 'numeric',
        'address': 'string',
        'salary': 'numeric'
    },
    'repair': { # Matches 'repair.csv'
        'mechSSN': 'string',
        'droneSN': 'string'
    },
    'rent': { # Matches 'rent.csv'
        'checkouts': 'string',
        'serialNum': 'string'
    },
    'transports': { # Matches 'transports.csv'
        'dSerialNum': 'string',
        'eSerialNum': 'string'
    }
}


def format_sql_value(value, sql_type):
    """
    Formats a value for insertion into an SQL statement.
    """
    if value is None:
        return "NULL"
    
    value_str = str(value).strip()
    
    if not value_str:
        return "NULL"

    if sql_type == 'numeric':
        try:
            float(value_str)
            return value_str
        except ValueError:
            print(f"Warning: Data-Schema mismatch. Expected numeric, but got '{value_str}'. Inserting NULL.")
            return "NULL"
    
    # sql_type == 'string'
    escaped_value = value_str.replace("'", "''")
    return f"'{escaped_value}'"


def generate_populate_script():
    """
    Reads CSV files from 'CSV_files' directory and generates a 'Populate.txt' SQL script.
    """
    csv_dir = 'CSV_files'
    output_file = 'Populate.txt'

    # This order MUST respect foreign key dependencies.
    csv_files = [
        # --- Group 1: No Dependencies ---
        'address.csv',
        'battery_type.csv',
        'drone_model.csv',
        'equip_model.csv',
        'order_price.csv',
        'warehouses.csv',
        'mechanics.csv',

        # --- Group 2: Depend on Group 1 ---
        'members.csv',      # Depends on 'address'
        'batteries.csv',    # Depends on 'battery_type'
        
        # --- Group 3: Depend on Groups 1 & 2 ---
        'orders.csv',       # Depends on 'order_price', 'members'
        'drones.csv',       # Depends on 'drone_model', 'warehouses', 'batteries'

        # --- Group 4: Depend on Groups 1, 2, & 3 ---
        'equipment.csv',    # Depends on 'equip_model', 'warehouses', 'orders', 'members'

        # --- Group 5: Join Tables / Final Dependencies ---
        'repair.csv',       # Depends on 'mechanics', 'drones'
        'reviews.csv',      # Depends on 'equipment', 'members'
        'transports.csv',   # Depends on 'drones', 'equipment'
        'rentals.csv',      # Depends on 'equipment', 'members'
        
        'rent.csv',         # Depends on 'rentals', 'equipment'
    ]


    try:
        with open(output_file, 'w', encoding='utf-8') as f_out:

            f_out.write("BEGIN TRANSACTION;\n\n")

            for csv_file in csv_files:
                table_name = os.path.splitext(csv_file)[0].lower()
                csv_path = os.path.join(csv_dir, csv_file)

                if not os.path.exists(csv_path):
                    print(f"Warning: '{csv_path}' not found. Skipping.")
                    continue
                
                if table_name not in SCHEMA_DEFINITION:
                    print(f"Warning: No schema definition found for table '{table_name}'. Skipping.")
                    continue
                
                table_schema = SCHEMA_DEFINITION[table_name]

                print(f"Processing {csv_file} -> {table_name} table...")
                
                try:
                    with open(csv_path, 'r', encoding='utf-8-sig') as f_in:

                        reader = csv.reader(f_in)

                        try:
                            header_raw = next(reader)
                        except StopIteration:
                            print(f"Warning: '{csv_file}' is empty. Skipping.")
                            continue
                        
                        header = [h.strip() for h in header_raw]

                        column_types = []
                        valid_header = True
                        try:
                            for col_name in header:
                                schema_key = next((k for k in table_schema.keys() if k.lower() == col_name.lower()), None)

                                if schema_key:
                                    column_types.append(table_schema[schema_key])
                                else:
                                    raise KeyError(col_name)

                        except KeyError as e:
                            print(f"Error: Column {e} in '{csv_file}' header not found in SCHEMA_DEFINITION for '{table_name}'. Skipping table.")
                            valid_header = False
                            continue
                        
                        if not valid_header:
                            continue

                        column_names = ', '.join([f'"{col}"' for col in header])
                        f_out.write(f"-- Populating {table_name}\n")

                        for row in reader:
                            if not any(row): 
                                continue

                            if len(row) != len(header):
                                print(f"Warning: Skipping row in {csv_file} due to column mismatch. Expected {len(header)}, got {len(row)}.")
                                continue
                            
                            formatted_values_list = []
                            for val, col_type in zip(row, column_types):
                                formatted_values_list.append(format_sql_value(val, col_type))
                            
                            formatted_values = ', '.join(formatted_values_list)
                            
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

