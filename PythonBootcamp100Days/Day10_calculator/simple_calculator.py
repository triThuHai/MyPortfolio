from art import logo

def add(n1, n2):
  return n1 + n2

def subtract(n1, n2):
  return n1 + n2

def multiply(n1, n2):
  return n1 * n2

def divide(n1, n2):
  if n2 != 0:
    return n1 / n2
    
operation = {"+": add, 
   "-": subtract, 
   "*": multiply, 
   "/": divide}

def calculator():  
  print(logo)
  
  num1 = float(input("1st number: "))
  for operation_key in operation:
    print(operation_key)
  while True:
    operation_key = input("Pick an operation from the line above: ")
    num2 = float(input("2nd number: "))        
    function = operation[operation_key]
    result = function(num1, num2)
    print(f"{num1} {operation_key} {num2} = {result}")
    continue_or_not = input(f"Type 'y' to continue calculating with {result}, or type 'n' to exit: ")
    if continue_or_not == "y":
      num1 = result
      continue #go back to the beginning of the loop 
    else:
      calculator() #recursion 
      break

calculator()
