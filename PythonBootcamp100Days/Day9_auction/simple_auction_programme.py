import os
from art_auction import logo

def prompt_inputs():
  name = input("What is your name? ").lower()
  bid = float(input("What is your bid? $"))
  return name, bid

def add_to_dict(name, bid): 
  dict = {}
  dict[name] = bid
  return dict

def highest_bidder(dict):
  price = 0
  while True:
    if dict[name] > price:
      price = dict[name]
      print(f"The highest bidder is ${dict[name]} with a bid of {name}")
      print("You are the winner!")
    break

def clear():
    os.system('cls' if os.name == 'nt' else 'clear')

while True:
  print(logo)
  name, bid = prompt_inputs()
  my_dict = add_to_dict(name, bid)
  continue_bid = input("Are there any other bidders? Type 'yes' or 'no': ").lower()
  if continue_bid == 'yes':
    clear()
  else:
    highest_bidder(my_dict)
    break
