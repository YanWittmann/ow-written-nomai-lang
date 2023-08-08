# Written Nomai generator

This (Java) project can generate an image with a spiral of symbols _resembling_ the text of the Nomai language from the
game [Outer Wilds](https://www.mobiusdigitalgames.com/outer-wilds). Since the game doesn't truly have a translatable
language, this project uses the conversion method published by
[u/36CornDogs](https://www.reddit.com/r/outerwilds/comments/15fj6yu/took_a_shot_at_a_nomai_writing_system/) on Reddit.

* [Introduction](#introduction)
* [Rules](#rules)
* [Conversion table](#conversion-table)
* [Process explained (demo)](#process-explained)
* [Licenses](#licenses)
* [Cool stuff to play around with in this project](#cool-stuff-to-play-around-with-in-this-project)

## Introduction

The language is made up of symbols, connected by lines. These chains of symbols are usually arranged in a spiral. A few
results of the generator can be seen below.

| ![img 1](doc/img/written_nomai_1691518406415.png) |                     ![img 2](doc/img/5dspvtk0uxgb1.png)                     |
|:-------------------------------------------------:|:---------------------------------------------------------------------------:|
|     I have 3287 apples but I wish I had 3288      | This translator will work on multiple sentences. See, this is a new branch. |

|                          ![img 3](doc/img/written_nomai_1691518552748.png)                          | ![img 4](doc/img/usk9dru34hgb1.png) |
|:---------------------------------------------------------------------------------------------------:|:-----------------------------------:|
| I have 3287 apples but I wish I had 3288. My friend has an extra apple you can have. Oh, thank you! |        I am fine, thank you!        |

## Rules

See the original post for a more detailed explanation:
[https://www.reddit.com/r/outerwilds/comments/15fj6yu/took_a_shot_at_a_nomai_writing_system/](https://www.reddit.com/r/outerwilds/comments/15fj6yu/took_a_shot_at_a_nomai_writing_system/)

- there are the following symbols: line, bend, square, pentagon, hexagon, octagon
- a "letter" is either a single symbol or two symbols combined
- the delimiter between letters is a single line
- the delimiter between words is a line connected to a dot connected to another line
- vowels branch off from the letter or word dot that came before it
- numbers branch off from the previous word dot
- this means, a word dot can have three lines coming out of it: consonant, vowel and number, and you must read them in
  the order "number", "vowel" and "consonant"

## Conversion table

If the letter you read is a consonant, start with the top row. If it is a vowel, start with the left column. Then,
use the other side to find the corresponding phoneme.

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

## Process explained

Let's take it step by step. Let's use `I have 3287 apples but I wish I had 3288. My friend has an extra apple you can
have.` as an example.

### Split the text into phonemes

We use the [cmudict](nomai-language-core/src/main/resources/ow-lang/cmudict.dict) from the CMU Pronouncing Dictionary
to look up the phonemes for each word. If a word does not exist in there, we split the word up into sub-tokens and
combine them back together later.

```
AY1 | HH, AE1, V | AE1, P, AH0, L, Z | B, AH1, T | AY1 | W, IH1, SH | AY1 | HH, AE1, D
M, AY1 | F, R, EH1, N, D | HH, AE1, Z | HH, AH0, Z | AE1, N | AH0, N | EH1, K, S, T, R, AH0 | AE1, P, AH0, L | Y, UW1 | K, AE1, N | K, AH0, N | HH, AE1, V
```

### Convert the phonemes to letters

These phonemes are then converted to letters using the conversion table above. This is a simple 1:1 mapping I created
for all available phonemes. This mapping is in
[cmudict-to-ow.txt](nomai-language-core/src/main/resources/ow-lang/cmudict-to-ow.txt).

The result is the following:

```
ay | h, aah, v | aah, p, uh, l, z | b, uh, t | ay | w, ih, sh | ay | h, aah, d | m, ay
f, r, eh, n, d | h, aah, z | aah, n | eh, k, s, t, r, uh | aah, p, uh, l | y, oo | k, aah, n | h, aah, v
```

### Creating a branching node tree from the letters

From this, we have to know in what directions to branch from what letter, since they are not drawn linearly, but in
three different lanes. I therefore split them up in three categories: consonants (C), vowels (V) and numbers (N).
Expand the spoiler below to see the result.

<details>
<summary>Expand me!</summary>

```
ROOT [V W]
  V [ay => SQUARE SQUARE SQUARE_SQUARE]

  ROOT [C]
    C [h => HEXAGON HEXAGON HEXAGON_HEXAGON]
      V [aah => SQUARE LINE LINE_SQUARE]
      C [v => HEXAGON _ HEXAGON]

        ROOT [V C N]
          V [aah => SQUARE LINE LINE_SQUARE]
          N [3 => OCTAGON _ OCTAGON]
            N [2 => HEXAGON _ HEXAGON]
              N [8 => HEXAGON PENTAGON PENTAGON_HEXAGON]
                N [7 => PENTAGON PENTAGON PENTAGON_PENTAGON]
          C [p => LINE _ LINE]
            V [uh => OCTAGON BEND BEND_OCTAGON]
            C [l => PENTAGON LINE LINE_PENTAGON]
              C [z => SQUARE BEND BEND_SQUARE]

                ROOT [C]
                  C [b => BEND _ BEND]
                    V [uh => OCTAGON BEND BEND_OCTAGON]
                    C [t => OCTAGON BEND BEND_OCTAGON]

                      ROOT [V W]
                        V [ay => SQUARE SQUARE SQUARE_SQUARE]

                        ROOT [C]
                          C [w => PENTAGON _ PENTAGON]
                            C [ih => _ BEND BEND]
                              C [sh => OCTAGON LINE LINE_OCTAGON]

                                ROOT [V W]
                                  V [ay => SQUARE SQUARE SQUARE_SQUARE]

                                  ROOT [C]
                                    C [h => HEXAGON HEXAGON HEXAGON_HEXAGON]
                                      V [aah => SQUARE LINE LINE_SQUARE]
                                      C [d => PENTAGON SQUARE SQUARE_PENTAGON]

                                        ROOT [C N]
                                          N [3 => OCTAGON _ OCTAGON]
                                            N [2 => HEXAGON _ HEXAGON]
                                              N [8 => HEXAGON PENTAGON PENTAGON_HEXAGON]
                                                N [8 => HEXAGON PENTAGON PENTAGON_HEXAGON]
                                          C [m => SQUARE _ SQUARE]
                                            V [ay => SQUARE SQUARE SQUARE_SQUARE]

                                            ROOT [C]
                                              C [f => OCTAGON _ OCTAGON]
                                                C [r => HEXAGON SQUARE SQUARE_HEXAGON]
                                                  V [eh => OCTAGON LINE LINE_OCTAGON]
                                                  C [n => SQUARE SQUARE SQUARE_SQUARE]
                                                    C [d => PENTAGON SQUARE SQUARE_PENTAGON]

                                                      ROOT [C]
                                                        C [h => HEXAGON HEXAGON HEXAGON_HEXAGON]
                                                          V [aah => SQUARE LINE LINE_SQUARE]
                                                          C [z => SQUARE BEND BEND_SQUARE]

                                                            ROOT [V C]
                                                              V [aah => SQUARE LINE LINE_SQUARE]
                                                              C [n => SQUARE SQUARE SQUARE_SQUARE]

                                                                ROOT [V C]
                                                                  V [eh => OCTAGON LINE LINE_OCTAGON]
                                                                  C [k => PENTAGON PENTAGON PENTAGON_PENTAGON]
                                                                    C [s => PENTAGON BEND BEND_PENTAGON]
                                                                      C [t => OCTAGON BEND BEND_OCTAGON]
                                                                        C [r => HEXAGON SQUARE SQUARE_HEXAGON]
                                                                          V [uh => OCTAGON BEND BEND_OCTAGON]

                                                                          ROOT [V C]
                                                                            V [aah => SQUARE LINE LINE_SQUARE]
                                                                            C [p => LINE _ LINE]
                                                                              V [uh => OCTAGON BEND BEND_OCTAGON]
                                                                              C [l => PENTAGON LINE LINE_PENTAGON]

                                                                                ROOT [C]
                                                                                  C [y => OCTAGON SQUARE SQUARE_OCTAGON]
                                                                                    V [oo => HEXAGON BEND BEND_HEXAGON]

                                                                                    ROOT [C]
                                                                                      C [k => PENTAGON PENTAGON PENTAGON_PENTAGON]
                                                                                        V [aah => SQUARE LINE LINE_SQUARE]
                                                                                        C [n => SQUARE SQUARE SQUARE_SQUARE]

                                                                                          ROOT [C]
                                                                                            C [h => HEXAGON HEXAGON HEXAGON_HEXAGON]
                                                                                              V [aah => SQUARE LINE LINE_SQUARE]
                                                                                              C [v => HEXAGON _ HEXAGON]
```

</details>

### Drawing the shapes

The first step of rendering the tree is to draw the letter shapes. You might be wondering why the shapes are in a
straight line and not in a curve. This is actually, because distributing the shapes and the transformations applied to
the shapes in the next few steps are a lot simpler when applying them in a regular grid.

This is done for each sentence individually, so we result in multiple such images. They will be combined later. This is
the result for the first sentence:

![Rendering Step 1](doc/img/rendering-step-1.png)

### Applying a Bézier curve

The next step picks a Bézier curve from a set of pre-defined curves depending on the length of the text. This curve is
then scaled and transformed in a few way to make the shapes all fit on the curve nicely.

Then, the curve is used to span a coordinate system, which is used to translate the shapes from the previous step onto
the curve.

<img alt="Rendering Step 2" height="400" src="doc/img/rendering-step-2.png"/>

### Connecting the letter symbols

Since each letter symbol still knows it's original node in the branch node tree, we can now connect the letter symbols
with lines to visualize the tree structure. This is done by connecting the closest two points of the letter symbols with
a check that they do not intersect with any other letter symbols or lines.

But not just any two points can be connected in this way, see the table below for an overview of the possible symbols
and their connection points.

<img alt="Rendering Step 3.1" height="400" src="doc/img/rendering-step-3-1.png"/><br>

<img alt="Rendering Step 3.2" height="300" src="doc/img/rendering-step-3-2.png"/>

### Combining the curves

Now we're pretty far already. The last step is to combine the curves from the previous steps into a single image. This
is done by combining the shapes such that the starting point of the next shape is at an offset of the first curve.

This offset is calculated based on several different factors, such as the length of both curves. Then, the curve is
angled a bit to make it look more natural.

In reality, before this step is performed, a few more checks are done. The most important one is: how many
intersection of shapes are there? If there are more than 0, up to 9 more iterations of all previous steps are performed
with a different seed and if the last iteration still has intersections, the best image with the best fit criteria is
chosen.

<img alt="Rendering Step 4" height="400" src="doc/img/rendering-step-4.png"/>

### Adding styling to the image

After all that work, it's time for decorating the image a bit. First, the image is overlayed a few times with copies of
itself that have been recolored, dilated and blurred.

<img alt="Rendering Step 5" height="400" src="doc/img/rendering-step-5.png"/>

### Adding a background

Then, a background from a set of pre-defined backgrounds is chosen. The available backgrounds are located in

- [src/main/resources/ow-lang/backgrounds](nomai-language-core/src/main/resources/ow-lang-renderer)
- [NomaiTextCompositor.java](nomai-language-core/src/main/java/de/yanwittmann/ow/lang/renderer/NomaiTextCompositor.java)

<img alt="Rendering Step 6" height="400" src="doc/img/rendering-step-6.png"/>

## Licenses

### cmudict

Source: [https://github.com/cmusphinx/cmudict](https://github.com/cmusphinx/cmudict)

This project includes the `cmudict.dict` file from the CMU Pronouncing Dictionary, which is covered by its own license.
The `cmudict.dict` file is located at
[src/main/resources/ow-lang/cmudict.dict](nomai-language-core/src/main/resources/ow-lang/cmudict.dict).

The license for the CMU Pronouncing Dictionary is located at
[src/main/resources/license/LICENSE-cmudict](nomai-language-core/src/main/resources/ow-lang/cmudict.dict).

## Cool stuff to play around with in this project

- [Live-edit the generated text and curve](nomai-language-core/src/test/java/de/yanwittmann/ow/lang/WrittenNomaiTextTokenizerTest.java)
  (main at bottom, also try setting the parameter to `true`!)
- [View all the shapes used to generate the spirals](nomai-language-core/src/test/java/de/yanwittmann/ow/lang/ShapeDefinitionVisualizer.java)
