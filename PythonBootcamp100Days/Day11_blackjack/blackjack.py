import random
import os
from art_blackjack import logo

def prompt_card():
    '''Draw a card number from list'''
    cards = [11, 2, 3, 4, 5, 6, 7, 8, 9, 10, 10, 10, 10]
    random_card = random.choice(cards)
    return random_card

def score_cal(card_list):
    '''Create a list of drawn card'''
    if sum(card_list) == 21 and len(card_list) == 2:
        return 0
    if sum(card_list) > 21 and 11 in card_list:
        card_list.remove(11)
        card_list.append(1)
    return sum(card_list)
    
def compare_score(player_score, computer_score):
    '''Compare player's and computer's score'''
    if player_score > 21 and computer_score > 21:
        return "You went over. You lose!"
    
    if player_score == computer_score:
        return "It's a draw"
    elif player_score > 21:
        return "You went over. You lose!"
    elif computer_score > 21:
        return "Computer went over. You win!"
    elif player_score == 0:
        return "You have a blackjack. You win!"
    elif compare_score == 0:
        return "Computer has a blackjack. You lose!"
    elif player_score > computer_score:
        return "You win!"
    else:
        return "You lose!"     
        
        
def run():
    print(logo)
    player_list = []
    computer_list = []
    #is_game_over = False
    
    for _ in range(2):#run loop twice 
        player_list.append(prompt_card())
        computer_list.append(prompt_card())
        
    while True:           
        player_score = score_cal(player_list)
        computer_score = score_cal(computer_list)
        print(f"Your card: {player_list}, current score: {score_cal(player_list)}")
        print(f"Computer's first card: {computer_list[0]}")
        if player_score > 21 or player_score == 0 or computer_score == 0 :
            break
        else:
            next_draw = input("Type 'y' to get another card, type 'n' to pass: ")
            if next_draw == "y":
                player_list.append(prompt_card())
            else:
                break #is_game_over = True
    
    while computer_score != 0 and computer_score < 17:
        computer_list.append(prompt_card())
        computer_score = score_cal(computer_list)
    print(f"Player: {player_list}, score: {player_score}")
    print(f"Computer: {computer_list}, score: {computer_score}")
    print(compare_score(player_score, computer_score))

def clear():
    os.system('cls' if os.name == 'nt' else 'clear')
        

while input("Do you want to play a game of Blackjack? Type 'y' or 'n': ") == "y":
    clear()
    run()      
        
    

        
                
        
    
    

            

