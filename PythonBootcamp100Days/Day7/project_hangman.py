import random
import hangman_words
import hangman_art
chosen_word = random.choice(hangman_words.word_list)
lives = 6 #Player's lives
end_of_game = False
track_list = [] #To track the words that have been guessed
print(hangman_art.logo)

#Create blanks
display = []
for _ in chosen_word:
  display.append("_")

while not end_of_game:
    guess = input("Guess a letter: ").lower()
    if guess in track_list:
        print(f"You have already guessed {guess}")      
    else:
        track_list.append(guess)
        if guess not in chosen_word:
            print(f"Your guess {guess} is not in the word. You lose a life.")
            lives -= 1
            if lives == 0:
                end_of_game = True
                print("You lose.")          
    
    #Add guess to position in display'
    for letter in chosen_word:
        if letter == guess:
            letter_index = chosen_word.index(letter)
            display[letter_index] = letter 
    
    #Check if user has got all letters.
    if "_" not in display:
        end_of_game = True
        print("You win.")
 
    #Join all the elements in the list and turn it into a String.
    print(f"{' '.join(display)}")
    print(hangman_art.stages[lives])
    