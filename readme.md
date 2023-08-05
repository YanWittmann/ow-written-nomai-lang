## Reference

- Original post:
  [https://www.reddit.com/r/outerwilds/comments/15fj6yu/took_a_shot_at_a_nomai_writing_system/](https://www.reddit.com/r/outerwilds/comments/15fj6yu/took_a_shot_at_a_nomai_writing_system/)
- Language definition: [https://i.redd.it/5ctmfqykbjfb1.jpg](https://i.redd.it/5ctmfqykbjfb1.jpg)

## Rules (reformulate them to be more precise)

- there are the following symbols: line, bend, square, pentagon, hexagon, octagon
- a "letter" us either a single symbol or two symbols combined
- the delimiter between letters is a single line
- the delimiter between words is a line connected to a dot connected to another line
- vowels branch off from the letter or word dot that came before it
- numbers branch off from the previous word dot
- this means, a word dot can have three lines coming out of it: consonant, vowel and number, and you must read them
  "number", "vowel", "consonant"

## Conversion table

| vowels v / consonants > |    | Line | Bend | Square | Pentagon | Hexagon | Octagon |
|-------------------------|----|------|------|--------|----------|---------|---------|
|                         |    | p    | b    | m      | w        | v       | f       |
| Line                    |    |      |      | th     | l        | ch      | sh      |
| Bend                    | ih |      |      | z      | s        | j       | t       |
| Square                  | 0  | aah  | iy   | ay / n | d        | r       | y       |
| Pentagon                | 1  | ah   | oh   | 4      | 7 / k    | g       | ng      |
| Hexagon                 | 2  | eee  | oo   | 5      | 8        | 10 / h  | oy      |
| Octagon                 | 3  | eh   | uh   | 6      | 9        |         |         |

Please note that I added `oy` to the table, as the closest would have been a combination from `oh` and `iy`.

## Licenses

### cmudict

Source: [https://github.com/cmusphinx/cmudict](https://github.com/cmusphinx/cmudict)

This project includes the `cmudict.dict` file from the CMU Pronouncing Dictionary, which is covered by its own license.
The `cmudict.dict` file is located at
[src/main/resources/ow-lang/cmudict.dict](src/main/resources/ow-lang/cmudict.dict).

The license for the CMU Pronouncing Dictionary is located at
[src/main/resources/license/LICENSE-cmudict](src/main/resources/ow-lang/cmudict.dict).

## Todo

This curve seems nice:

    {{446, 709}, {270, 215}, {646, 1}, {896, 452}, {591, 482}}
    {{0, 0}, {-176, -494}, {200, -708}, {450, -257}, {145, -227}}
