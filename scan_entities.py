import os
import re

def camel_to_snake(name):
    s1 = re.sub('(.)([A-Z][a-z]+)', r'\1_\2', name)
    return re.sub('([a-z0-9])([A-Z])', r'\1_\2', s1).lower()

def load_db_schema(file_path):
    table_columns = {}
    content = ""
    
    # Try different encodings
    for encoding in ['utf-8', 'utf-16', 'gbk', 'mbcs']:
        try:
            with open(file_path, 'r', encoding=encoding) as f:
                content = f.read()
            break
        except UnicodeDecodeError:
            continue
            
    if not content:
        return {}
        
    lines = content.split('\n')
    # Skip header if it exists (usually row 1)
    if len(lines) > 0 and 'TABLE_NAME' in lines[0]:
        lines = lines[1:]
        
    for line in lines:
            parts = line.strip().split('\t')
            if len(parts) >= 2:
                table_name = parts[0]
                column_name = parts[1]
                if table_name not in table_columns:
                    table_columns[table_name] = set()
                table_columns[table_name].add(column_name)
    return table_columns

def parse_java_entity(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
        
    # Extract class name
    class_match = re.search(r'public class (\w+)', content)
    if not class_match:
        return None, []
    
    class_name = class_match.group(1)
    
    # Check for @TableName annotation to get exact table name
    table_name_match = re.search(r'@TableName\("(\w+)"\)', content)
    table_name = table_name_match.group(1) if table_name_match else camel_to_snake(class_name)
    
    # Iterate lines to find fields and check for exclusions
    lines = content.split('\n')
    valid_fields = []
    
    is_excluded = False
    for line in lines:
        if '@TableField' in line and 'exist = false' in line:
            is_excluded = True
            continue
        
        # Check for field definition: private Type name;
        if line.strip().startswith('private ') and 'static' not in line:
            # Regex to capture field name. Handles generic types roughly.
            # Matches: private List<String> names; -> names
            match = re.search(r'private\s+(?:[\w<>,?\[\] ]+\s+)+(\w+)\s*;', line)
            if match:
                field_name = match.group(1)
                if not is_excluded:
                    valid_fields.append(camel_to_snake(field_name))
                is_excluded = False # Reset for next field
        elif line.strip() and not line.strip().startswith('@'):
             # If line is not an annotation and not a field (and not empty), reset exclusion
             is_excluded = False
             
    return table_name, valid_fields

def main():
    db_schema_file = 'db_schema.txt'
    if not os.path.exists(db_schema_file):
        print(f"Error: {db_schema_file} not found.")
        return

    db_schema = load_db_schema(db_schema_file)
    domain_dir = r'd:\MyCode\im\ruoyi-im-api\src\main\java\com\ruoyi\im\domain'
    
    print(f"{'Table/Entity':<30} | {'Status':<10} | {'Details'}")
    print("-" * 100)
    
    for filename in os.listdir(domain_dir):
        if not filename.endswith('.java'):
            continue
            
        file_path = os.path.join(domain_dir, filename)
        try:
            table_name, fields = parse_java_entity(file_path)
        except Exception as e:
            print(f"Error parsing {filename}: {e}")
            continue
        
        if not table_name:
            continue
            
        if table_name not in db_schema:
            # Ignore some known non-table entities or DTOs if they ended up here
            # But in domain package usually they map to tables
            print(f"{table_name:<30} | Missing    | Table not found in DB")
            continue
            
        db_cols = db_schema[table_name]
        
        missing_in_db = []
        for f in fields:
            if f not in db_cols:
                # Filter out some common non-db fields if detection failed
                if f not in ['serial_version_uid', 'params']: 
                    missing_in_db.append(f)
        
        if missing_in_db:
            print(f"{table_name:<30} | Mismatch   | Code has fields not in DB: {', '.join(missing_in_db)}")

if __name__ == '__main__':
    main()
