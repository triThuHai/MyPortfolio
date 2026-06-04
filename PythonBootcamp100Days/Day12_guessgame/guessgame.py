#Number Guessing Game Objectives:

# Include an ASCII art logo.
# Allow the player to submit a guess for a number between 1 and 100.
# Check user's guess against actual answer. Print "Too high." or "Too low." depending on the user's answer. 
# If they got the answer correct, show the actual answer to the player.
# Track the number of turns remaining.
# If they run out of turns, provide feedback to the player. 
# Include two different difficulty levels (e.g., 10 guesses in easy mode, only 5 guesses in hard mode).
import random
from art_guessgame import logo


print(logo)
print("Welome to the Number Guessing Game!")
print("I'm thinking of a number between 1 and 100.")
start_game = input("Choose a difficulty, easy or hard: ")

EASY_LEVEL = 10
HARD_LEVLE = 5

def level(start_game):
  if start_game == "easy":
    return EASY_LEVEL
  elif start_game == "hard":
    return HARD_LEVLE
  
def play_game():
  lives = level(start_game)
  while True:
      print(f"You have {lives} attempts remaining to guess the number.")
      number = int(input("Make a guess: "))
      if number == rand_num:
        print(f"You got it! The answer was {rand_num}")
        break
      else:
        lives -= 1
        if lives != 0:
          if number > rand_num:
            print("Too high.")
          else:
            print("Too low.")
          print("Guess again.")  
        else:
          print("You've run out of guesses, you lose.")
          break      
  
rand_num = random.randint(1,100)
print(rand_num)
play_game()
  


  
  
  


