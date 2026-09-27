# Ingredients available for recipes

Reference for picking ingredients when adding content. Everything here was read out of the actual
jars, not from memory.

The mod id for Endless Backrooms is `endless_backrooms`, for Farmer's Delight Refabricated it is
`farmersdelight`.

## Prefer tags over item ids

A tag makes one recipe accept a whole category of items, which is what Farmer's Delight itself
does — its own `onion_soup` asks for `c:crops/onion`, not for the onion item. Use an id only when
the ingredient genuinely must be one specific item.

**Endless Backrooms**

| Tag                                | Contains                                          |
| ---------------------------------- | ------------------------------------------------- |
| `endless_backrooms:almond_water`   | all four coloured almond waters                    |
| `endless_backrooms:mushroom_material` | mushroom material                              |

Note there is **no** un-prefixed almond water item; the four coloured bottles are the only forms,
so `endless_backrooms:almond_water` is the only way to accept "any almond water".

**Farmer's Delight** — the `c:` convention tags it ships:

| Tag                        | Contains                                            |
| -------------------------- | --------------------------------------------------- |
| `c:foods/vegetables`       | beetroot, carrot, onion, potato, tomato              |
| `c:mushrooms`              | red and brown mushroom                               |
| `c:salad_ingredients`      | cabbage                                              |
| `c:foods/raw_meats`        | raw bacon, beef, chicken, mutton, pork               |
| `c:foods/cooked_meats`     | cooked bacon, beef, chicken, eggs, mutton, pork      |
| `c:foods/raw_fishes`       | cod, salmon, tropical fish                           |
| `c:foods/cooked_fishes`    | cod, salmon                                          |
| `c:grains`                 | wheat, rice                                          |
| `c:milks`                  | milk buckets, milk bottles                           |
| `c:foods`                  | everything edible above, plus breads, doughs, pastas |
| `c:crops/carrot`, `c:crops/onion`, `c:crops/rice`, `c:crops/tomato`, `c:crops/cabbage` | that one crop |
| `c:tools/knives`           | all five knives                                      |

There are also category tags for foods already in a finished state, useful for "any meal" style
recipes: `farmersdelight:meals`, `farmersdelight:snacks`, `farmersdelight:sweets`,
`farmersdelight:drinks`, `farmersdelight:feasts`, `farmersdelight:pies`.

## Endless Backrooms

31 items. The ones that are food or plausibly edible:

| Item                                   | Chinese          | Notes                                            |
| -------------------------------------- | ---------------- | ------------------------------------------------ |
| `endless_backrooms:mushroom_stew`      | 蘑菇炖           | tooltip hints it is better cooked                |
| `endless_backrooms:cooked_mushroom_stew` | 熟蘑菇炖       | the cooked form                                  |
| `endless_backrooms:raw_scit`           | 生旱虾           | raw; `cooked_scit` is 熟旱虾                      |
| `endless_backrooms:cooked_scit`        | 熟旱虾           |                                                  |
| `endless_backrooms:royal_rations`      | 皇家口粮         | 4 uses, then returns a bowl; addictive           |
| `endless_backrooms:moth_jelly`         | 蛾冻             | grants Saturation + Moth Pheromone; addictive    |
| `endless_backrooms:moth_jelly_material` | 蛾冻原料        | not food, an ingredient                          |
| `endless_backrooms:gray_almond_water`  | 灰瓶杏仁水       | in the almond water tag                          |
| `endless_backrooms:green_almond_water` | 绿瓶杏仁水       | in the almond water tag                          |
| `endless_backrooms:red_almond_water`   | 红瓶杏仁水       | in the almond water tag                          |
| `endless_backrooms:blue_almond_water`  | 蓝瓶杏仁水       | in the almond water tag                          |
| `endless_backrooms:firesalt`           | 火盐             | tooltip mentions weapon or fuel use              |
| `endless_backrooms:nutrient_growth_liquid` | 营养促生液   |                                                  |
| `endless_backrooms:fungal_culture_fluid` | 真菌培养液     |                                                  |
| `endless_backrooms:corruption_liquid`  | 腐化液           |                                                  |
| `endless_backrooms:blue_lightning_in_a_bottle` | 蓝色瓶装闪电 | tooltip hints at a use                    |
| `endless_backrooms:black_lightning_in_a_bottle` | 黑色瓶装闪电 |                                          |

Non-food but potentially interesting as crafting parts: `level_zero_carpet` (Level 0 地毯, already
used for Fried Carpet), `level_zero_wallpaper` (Level 0 墙皮), `wood_fiber` / `rough_wood_fiber` /
`wet_wood_fiber` (木纤维), `paper_pulp` (纸浆), `scrap_iron` (铁片), `rusty_iron_powder` (锈铁粉),
`wire` (电线), `battery` (电池), `gauge` (仪表), `pebble` (石子), `moth_cage` (蛾笼),
`utility_knife` (美工刀).

Mob effects this mod adds, which any of your items can grant: `withdrawal` (戒断反应),
`wretched_cycle` (悲尸循环), `escape` (逃逸), `moth_pheromone` (蛾信息素). Only the last one is a buff.

## Farmer's Delight

Raw ingredients, the ones worth building recipes from:

| Item                              | Chinese      |
| --------------------------------- | ------------ |
| `farmersdelight:onion`            | 洋葱          |
| `farmersdelight:tomato`           | 番茄          |
| `farmersdelight:cabbage`          | 卷心菜        |
| `farmersdelight:rice`             | 稻米          |
| `farmersdelight:cabbage_leaf`     | 卷心菜叶      |
| `farmersdelight:tomato_sauce`     | 番茄酱        |
| `farmersdelight:minced_beef`      | 牛肉馅        |
| `farmersdelight:beef_patty`       | 牛肉饼        |
| `farmersdelight:bacon`            | 生培根        |
| `farmersdelight:chicken_cuts`     | 生鸡肉丁      |
| `farmersdelight:cod_slice`        | 生鳕鱼片      |
| `farmersdelight:salmon_slice`     | 生鲑鱼片      |
| `farmersdelight:mutton_chops`     | 生羊排        |
| `farmersdelight:ham`              | 火腿          |
| `farmersdelight:smoked_ham`       | 烟熏火腿      |
| `farmersdelight:wheat_dough`      | 面团          |
| `farmersdelight:raw_pasta`        | 生意面        |
| `farmersdelight:pie_crust`        | 馅饼酥皮      |
| `farmersdelight:milk_bottle`      | 奶瓶          |
| `farmersdelight:pumpkin_slice`    | 南瓜片        |
| `farmersdelight:tree_bark`        | 树皮          |
| `farmersdelight:straw`            | 草秆          |
| `farmersdelight:rotten_tomato`    | 烂番茄        |
| `farmersdelight:earthworm`        | 蚯蚓          |

Cooked counterparts exist for the meats and fish (`cooked_bacon`, `cooked_chicken_cuts`,
`cooked_cod_slice`, `cooked_salmon_slice`, `cooked_mutton_chops`).

## Vanilla

Useful because they need no other mod: `minecraft:carrot`, `potato`, `beetroot`,
`red_mushroom`, `brown_mushroom`, `sugar`, `egg`, `wheat`, `sweet_berries`, `glow_berries`,
`melon_slice`, `pumpkin`, `honey_bottle`, `kelp`, `dried_kelp`, `cod`, `salmon`,
`tropical_fish`, `beef`, `porkchop`, `chicken`, `mutton`, `rabbit`, `bowl`, `glass_bottle`,
`milk_bucket`, `cocoa_beans`, `apple`, `golden_apple`.

Note that vanilla's own `bowl` is what every bowl meal in this mod returns, via
`craftRemainder(Items.BOWL)`.
