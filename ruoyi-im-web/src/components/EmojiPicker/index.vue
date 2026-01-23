<template>
  <div v-if="visible" class="emoji-picker">
    <div class="emoji-tabs">
      <div
        v-for="tab in tabs"
        :key="tab.type"
        class="emoji-tab"
        :class="{ active: activeTab === tab.type }"
        @click="activeTab = tab.type"
      >
        {{ tab.label }}
      </div>
    </div>

    <div class="emoji-grid">
      <div
        v-for="emoji in currentEmojis"
        :key="emoji.char"
        class="emoji-item"
        @click="selectEmoji(emoji.char)"
      >
        {{ emoji.char }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['select'])

const activeTab = ref('smile')

// 表情分类标签
const tabs = [
  { type: 'smile', label: '😀' },
  { type: 'hand', label: '👍' },
  { type: 'animal', label: '🐶' },
  { type: 'food', label: '🍎' },
  { type: 'activity', label: '⚽' },
  { type: 'object', label: '❤️' }
]

// 表情数据
const emojiData = {
  smile: [
    { char: '😀', keywords: ['开心', '笑脸'] },
    { char: '😄', keywords: ['笑', '快乐'] },
    { char: '😁', keywords: ['咧嘴笑'] },
    { char: '😆', keywords: ['大笑'] },
    { char: '😅', keywords: ['汗'] },
    { char: '🤣', keywords: ['笑哭'] },
    { char: '😂', keywords: ['笑哭'] },
    { char: '🙂', keywords: ['微笑'] },
    { char: '🙃', keywords: ['倒脸'] },
    { char: '😉', keywords: ['眨眼'] },
    { char: '😊', keywords: ['开心', '幸福'] },
    { char: '😇', keywords: ['天使'] },
    { char: '🥰', keywords: ['爱心眼'] },
    { char: '😍', keywords: ['爱'] },
    { char: '🤩', keywords: ['星星眼'] },
    { char: '😘', keywords: ['亲亲'] },
    { char: '😗', keywords: ['吻'] },
    { char: '😙', keywords: ['笑吻'] },
    { char: '😚', keywords: ['闭眼吻'] },
    { char: '😋', keywords: ['美味'] },
    { char: '😛', keywords: ['吐舌头'] },
    { char: '😜', keywords: ['调皮'] },
    { char: '🤪', keywords: ['搞怪'] },
    { char: '😝', keywords: ['吐舌头'] },
    { char: '🤑', keywords: ['钱'] },
    { char: '🤗', keywords: ['拥抱'] },
    { char: '🤭', keywords: ['偷笑'] },
    { char: '🤫', keywords: ['嘘'] },
    { char: '🤔', keywords: ['思考'] },
    { char: '🤐', keywords: ['闭嘴'] },
    { char: '🤨', keywords: ['疑惑'] },
    { char: '😐', keywords: ['面无表情'] },
    { char: '😑', keywords: ['无语'] },
    { char: '😶', keywords: ['沉默'] },
    { char: '😏', keywords: ['得意'] },
    { char: '😒', keywords: ['不爽'] },
    { char: '🙄', keywords: ['翻白眼'] },
    { char: '😬', keywords: ['尴尬'] },
    { char: '🤥', keywords: ['说谎'] },
    { char: '😌', keywords: ['放松'] },
    { char: '😔', keywords: ['沮丧'] },
    { char: '😪', keywords: ['困'] },
    { char: '🤤', keywords: ['流口水'] },
    { char: '😴', keywords: ['睡觉'] },
    { char: '😷', keywords: ['生病'] },
    { char: '🤒', keywords: ['感冒'] },
    { char: '🤕', keywords: ['受伤'] },
    { char: '🤢', keywords: ['恶心'] },
    { char: '🤮', keywords: ['呕吐'] },
    { char: '🤧', keywords: ['打喷嚏'] },
    { char: '🥵', keywords: ['热'] },
    { char: '🥶', keywords: ['冷'] },
    { char: '🥴', keywords: ['头晕'] },
    { char: '😵', keywords: ['晕'] },
    { char: '🤯', keywords: ['爆炸'] },
    { char: '🤠', keywords: ['牛仔'] },
    { char: '🥳', keywords: ['派对'] },
    { char: '🥸', keywords: ['眼镜'] },
    { char: '😎', keywords: ['墨镜'] },
    { char: '🤓', keywords: ['书呆子'] },
    { char: '🧐', keywords: ['单片眼镜'] },
    { char: '😕', keywords: ['困惑'] },
    { char: '😟', keywords: ['担心'] },
    { char: '🙁', keywords: ['不开心'] },
    { char: '☹️', keywords: ['难过'] },
    { char: '😮', keywords: ['惊讶'] },
    { char: '😯', keywords: ['惊讶'] },
    { char: '😲', keywords: ['震惊'] },
    { char: '😳', keywords: ['害羞'] },
    { char: '🥺', keywords: ['可怜'] },
    { char: '😦', keywords: ['担心'] },
    { char: '😧', keywords: ['痛苦'] },
    { char: '😨', keywords: ['恐惧'] },
    { char: '😰', keywords: ['焦虑'] },
    { char: '😥', keywords: ['失望'] },
    { char: '😢', keywords: ['哭'] },
    { char: '😭', keywords: ['大哭'] },
    { char: '😱', keywords: ['尖叫'] },
    { char: '😖', keywords: ['纠结'] },
    { char: '😣', keywords: ['痛苦'] },
    { char: '😞', keywords: ['失望'] },
    { char: '😓', keywords: ['汗颜'] },
    { char: '😩', keywords: ['疲惫'] },
    { char: '😫', keywords: ['疲倦'] },
    { char: '🥱', keywords: ['打哈欠'] },
    { char: '😤', keywords: ['生气'] },
    { char: '😡', keywords: ['愤怒'] },
    { char: '😠', keywords: ['生气'] },
    { char: '🤬', keywords: ['骂人'] },
    { char: '😈', keywords: ['恶魔'] },
    { char: '👿', keywords: ['魔鬼'] },
    { char: '💀', keywords: ['骷髅'] },
    { char: '☠️', keywords: ['海盗旗'] },
    { char: '💩', keywords: ['便便'] },
    { char: '🤡', keywords: ['小丑'] },
    { char: '👹', keywords: ['鬼'] },
    { char: '👺', keywords: ['怪物'] },
    { char: '👻', keywords: ['鬼'] },
    { char: '👽', keywords: ['外星人'] },
    { char: '👾', keywords: ['外星人'] },
    { char: '🤖', keywords: ['机器人'] },
    { char: '😺', keywords: ['猫'] },
    { char: '😸', keywords: ['开心猫'] },
    { char: '😹', keywords: ['笑猫'] },
    { char: '😻', keywords: ['爱心眼猫'] },
    { char: '😼', keywords: ['调皮猫'] },
    { char: '😽', keywords: ['吻猫'] },
    { char: '🙀', keywords: ['恐惧猫'] },
    { char: '😿', keywords: ['哭猫'] },
    { char: '😾', keywords: ['生气猫'] },
    { char: '🙈', keywords: ['不看'] },
    { char: '🙉', keywords: ['不听'] },
    { char: '🙊', keywords: ['不说'] },
    { char: '👋', keywords: ['挥手'] },
    { char: '🤚', keywords: ['举手'] },
    { char: '🖐️', keywords: ['手掌'] },
    { char: '✋', keywords: ['手掌'] },
    { char: '🖖', keywords: ['瓦肯举手礼'] },
    { char: '👌', keywords: ['OK'] },
    { char: '🤌', keywords: ['捏手指'] },
    { char: '🤏', keywords: ['捏手指'] },
    { char: '✌️', keywords: ['V字'] },
    { char: '🤞', keywords: ['交叉手指'] },
    { char: '🤟', keywords: ['爱意手势'] },
    { char: '🤘', keywords: ['摇滚'] },
    { char: '🤙', keywords: ['呼啦'] },
    { char: '👈', keywords: ['左指'] },
    { char: '👉', keywords: ['右指'] },
    { char: '👆', keywords: ['上指'] },
    { char: '🖕', keywords: ['中指'] },
    { char: '👇', keywords: ['下指'] },
    { char: '☝️', keywords: ['上指'] },
    { char: '👍', keywords: ['赞'] },
    { char: '👎', keywords: ['踩'] },
    { char: '✊', keywords: ['握拳'] },
    { char: '👊', keywords: ['出拳'] },
    { char: '🤛', keywords: ['左拳头碰'] },
    { char: '🤜', keywords: ['右拳头碰'] },
    { char: '👏', keywords: ['鼓掌'] },
    { char: '🙌', keywords: ['举手'] },
    { char: '👐', keywords: ['张开手'] },
    { char: '🤲', keywords: ['张开手掌'] },
    { char: '🤝', keywords: ['握手'] },
    { char: '🙏', keywords: ['祈祷'] }
  ],
  hand: [
    { char: '✋', keywords: ['手掌'] },
    { char: '🤚', keywords: ['举手'] },
    { char: '🖐️', keywords: ['手掌'] },
    { char: '✊', keywords: ['握拳'] },
    { char: '👊', keywords: ['出拳'] },
    { char: '🤛', keywords: ['左拳头碰'] },
    { char: '🤜', keywords: ['右拳头碰'] },
    { char: '👌', keywords: ['OK'] },
    { char: '🤌', keywords: ['捏手指'] },
    { char: '🤏', keywords: ['捏手指'] },
    { char: '✌️', keywords: ['V字'] },
    { char: '🤞', keywords: ['交叉手指'] },
    { char: '🤟', keywords: ['爱意手势'] },
    { char: '🤘', keywords: ['摇滚'] },
    { char: '🤙', keywords: ['呼啦'] },
    { char: '👈', keywords: ['左指'] },
    { char: '👉', keywords: ['右指'] },
    { char: '👆', keywords: ['上指'] },
    { char: '🖕', keywords: ['中指'] },
    { char: '👇', keywords: ['下指'] },
    { char: '☝️', keywords: ['上指'] },
    { char: '👍', keywords: ['赞'] },
    { char: '👎', keywords: ['踩'] },
    { char: '👏', keywords: ['鼓掌'] },
    { char: '🙌', keywords: ['举手'] },
    { char: '👐', keywords: ['张开手'] },
    { char: '🤲', keywords: ['张开手掌'] },
    { char: '🤝', keywords: ['握手'] },
    { char: '🙏', keywords: ['祈祷'] },
    { char: '✍️', keywords: ['写字'] },
    { char: '💅', keywords: ['美甲'] },
    { char: '🤳', keywords: ['自拍'] },
    { char: '💪', keywords: ['肌肉'] },
    { char: '🦾', keywords: ['机械臂'] },
    { char: '🦿', keywords: ['机械腿'] },
    { char: '🦵', keywords: ['腿'] },
    { char: '🦶', keywords: ['脚'] },
    { char: '👂', keywords: ['耳朵'] },
    { char: '🦻', keywords: ['助听器'] },
    { char: '👃', keywords: ['鼻子'] },
    { char: '🧠', keywords: ['大脑'] },
    { char: '🦷', keywords: ['牙齿'] },
    { char: '🦴', keywords: ['骨头'] },
    { char: '👀', keywords: ['眼睛'] },
    { char: '👁️', keywords: ['眼睛'] },
    { char: '👅', keywords: ['舌头'] },
    { char: '👄', keywords: ['嘴'] }
  ],
  animal: [
    { char: '🐶', keywords: ['狗'] },
    { char: '🐱', keywords: ['猫'] },
    { char: '🐭', keywords: ['老鼠'] },
    { char: '🐹', keywords: ['仓鼠'] },
    { char: '🐰', keywords: ['兔子'] },
    { char: '🦊', keywords: ['狐狸'] },
    { char: '🐻', keywords: ['熊'] },
    { char: '🐼', keywords: ['熊猫'] },
    { char: '🐨', keywords: ['考拉'] },
    { char: '🐯', keywords: ['老虎'] },
    { char: '🦁', keywords: ['狮子'] },
    { char: '🐮', keywords: ['牛'] },
    { char: '🐷', keywords: ['猪'] },
    { char: '🐸', keywords: ['青蛙'] },
    { char: '🐵', keywords: ['猴子'] },
    { char: '🐔', keywords: ['鸡'] },
    { char: '🐧', keywords: ['企鹅'] },
    { char: '🐦', keywords: ['鸟'] },
    { char: '🦆', keywords: ['鸭子'] },
    { char: '🦅', keywords: ['老鹰'] },
    { char: '🦉', keywords: ['猫头鹰'] },
    { char: '🦇', keywords: ['蝙蝠'] },
    { char: '🐺', keywords: ['狼'] },
    { char: '🐗', keywords: ['野猪'] },
    { char: '🐴', keywords: ['马'] },
    { char: '🦄', keywords: ['独角兽'] },
    { char: '🐝', keywords: ['蜜蜂'] },
    { char: '🐛', keywords: ['毛虫'] },
    { char: '🦋', keywords: ['蝴蝶'] },
    { char: '🐌', keywords: ['蜗牛'] },
    { char: '🐞', keywords: ['瓢虫'] },
    { char: '🐜', keywords: ['蚂蚁'] },
    { char: '🦟', keywords: ['蚊子'] },
    { char: '🦗', keywords: ['蟋蟀'] },
    { char: '🕷️', keywords: ['蜘蛛'] },
    { char: '🦂', keywords: ['蝎子'] },
    { char: '🦠', keywords: ['细菌'] },
    { char: '💐', keywords: ['花'] },
    { char: '🌸', keywords: ['樱花'] },
    { char: '💮', keywords: ['白花'] },
    { char: '🏵️', keywords: ['玫瑰'] },
    { char: '🌹', keywords: ['玫瑰'] },
    { char: '🥀', keywords: ['枯萎的花'] },
    { char: '🌺', keywords: ['花'] },
    { char: '🌻', keywords: ['向日葵'] },
    { char: '🌼', keywords: ['花'] },
    { char: '🌷', keywords: ['郁金香'] },
    { char: '🌱', keywords: ['幼苗'] },
    { char: '🪴', keywords: ['盆栽'] },
    { char: '🌲', keywords: ['松树'] },
    { char: '🌳', keywords: ['树'] },
    { char: '🌴', keywords: ['棕榈树'] },
    { char: '🌵', keywords: ['仙人掌'] },
    { char: '🌾', keywords: ['稻谷'] },
    { char: '🌿', keywords: ['草'] },
    { char: '☘️', keywords: ['三叶草'] },
    { char: '🍀', keywords: ['四叶草'] }
  ],
  food: [
    { char: '🍎', keywords: ['苹果'] },
    { char: '🍐', keywords: ['梨'] },
    { char: '🍊', keywords: ['橘子'] },
    { char: '🍋', keywords: ['柠檬'] },
    { char: '🍌', keywords: ['香蕉'] },
    { char: '🍉', keywords: ['西瓜'] },
    { char: '🍇', keywords: ['葡萄'] },
    { char: '🍓', keywords: ['草莓'] },
    { char: '🫐', keywords: ['蓝莓'] },
    { char: '🍈', keywords: ['瓜'] },
    { char: '🍒', keywords: ['樱桃'] },
    { char: '🍑', keywords: ['桃子'] },
    { char: '🥭', keywords: ['芒果'] },
    { char: '🍍', keywords: ['菠萝'] },
    { char: '🥥', keywords: ['椰子'] },
    { char: '🥝', keywords: ['猕猴桃'] },
    { char: '🍅', keywords: ['西红柿'] },
    { char: '🍆', keywords: ['茄子'] },
    { char: '🥑', keywords: ['牛油果'] },
    { char: '🥦', keywords: ['西兰花'] },
    { char: '🥬', keywords: ['蔬菜'] },
    { char: '🥒', keywords: ['黄瓜'] },
    { char: '🌶️', keywords: ['辣椒'] },
    { char: '🫑', keywords: ['甜椒'] },
    { char: '🌽', keywords: ['玉米'] },
    { char: '🥕', keywords: ['胡萝卜'] },
    { char: '🧄', keywords: ['大蒜'] },
    { char: '🧅', keywords: ['洋葱'] },
    { char: '🥔', keywords: ['土豆'] },
    { char: '🍠', keywords: ['红薯'] },
    { char: '🥐', keywords: ['牛角包'] },
    { char: '🥯', keywords: ['贝果'] },
    { char: '🍞', keywords: ['面包'] },
    { char: '🥖', keywords: ['法棍'] },
    { char: '🥨', keywords: ['椒盐卷饼'] },
    { char: '🧀', keywords: ['奶酪'] },
    { char: '🥚', keywords: ['鸡蛋'] },
    { char: '🍳', keywords: ['煎蛋'] },
    { char: '🧈', keywords: ['黄油'] },
    { char: '🥞', keywords: ['煎饼'] },
    { char: '🧇', keywords: ['华夫饼'] },
    { char: '🥓', keywords: ['培根'] },
    { char: '🥩', keywords: ['牛排'] },
    { char: '🍗', keywords: ['鸡腿'] },
    { char: '🍖', keywords: ['带骨肉'] },
    { char: '🌭', keywords: ['热狗'] },
    { char: '🍔', keywords: ['汉堡'] },
    { char: '🍟', keywords: ['薯条'] },
    { char: '🍕', keywords: ['披萨'] },
    { char: '🥪', keywords: ['三明治'] },
    { char: '🥙', keywords: ['卷饼'] },
    { char: '🧆', keywords: ['丸子'] },
    { char: '🌮', keywords: ['墨西哥卷'] },
    { char: '🌯', keywords: ['墨西哥饼'] },
    { char: '🫔', keywords: ['粽子'] },
    { char: '🥗', keywords: ['沙拉'] },
    { char: '🥘', keywords: ['海鲜饭'] },
    { char: '🥫', keywords: ['罐头'] },
    { char: '🍝', keywords: ['意大利面'] },
    { char: '🍜', keywords: ['面条'] },
    { char: '🍲', keywords: ['火锅'] },
    { char: '🍛', keywords: ['咖喱饭'] },
    { char: '🍣', keywords: ['寿司'] },
    { char: '🍱', keywords: ['便当'] },
    { char: '🥟', keywords: ['饺子'] },
    { char: '🦪', keywords: ['生蚝'] },
    { char: '🍤', keywords: ['炸虾'] },
    { char: '🍙', keywords: ['饭团'] },
    { char: '🍚', keywords: ['米饭'] },
    { char: '🍘', keywords: ['仙贝'] },
    { char: '🍥', keywords: ['鱼糕'] },
    { char: '🥠', keywords: ['幸运饼干'] },
    { char: '🥮', keywords: ['月饼'] },
    { char: '🍢', keywords: ['关东煮'] },
    { char: '🍡', keywords: ['团子'] },
    { char: '🍧', keywords: ['刨冰'] },
    { char: '🍨', keywords: ['冰淇淋'] },
    { char: '🍦', keywords: ['雪糕'] },
    { char: '🥧', keywords: ['派'] },
    { char: '🧁', keywords: ['纸杯蛋糕'] },
    { char: '🍰', keywords: ['蛋糕'] },
    { char: '🎂', keywords: ['生日蛋糕'] },
    { char: '🍮', keywords: ['布丁'] },
    { char: '🍭', keywords: ['棒棒糖'] },
    { char: '🍬', keywords: ['糖果'] },
    { char: '🍫', keywords: ['巧克力'] },
    { char: '🍿', keywords: ['爆米花'] },
    { char: '🍩', keywords: ['甜甜圈'] },
    { char: '🍪', keywords: ['饼干'] },
    { char: '🌰', keywords: ['栗子'] },
    { char: '🥜', keywords: ['花生'] },
    { char: '🍯', keywords: ['蜂蜜'] },
    { char: '🥛', keywords: ['牛奶'] },
    { char: '🍼', keywords: ['奶瓶'] },
    { char: '☕', keywords: ['咖啡'] },
    { char: '🍵', keywords: ['茶'] },
    { char: '🧃', keywords: ['果汁'] },
    { char: '🥤', keywords: ['饮料'] },
    { char: '🧋', keywords: ['奶茶'] },
    { char: '🫖', keywords: ['茶壶'] },
    { char: '🍶', keywords: ['清酒'] },
    { char: '🍺', keywords: ['啤酒'] },
    { char: '🍻', keywords: ['啤酒'] },
    { char: '🥂', keywords: ['碰杯'] },
    { char: '🍷', keywords: ['红酒'] },
    { char: '🥃', keywords: ['威士忌'] },
    { char: '🍸', keywords: ['鸡尾酒'] },
    { char: '🍹', keywords: ['热带饮料'] },
    { char: '🧊', keywords: ['冰块'] },
    { char: '🥢', keywords: ['筷子'] },
    { char: '🍽️', keywords: ['餐具'] },
    { char: '🍴', keywords: ['刀叉'] },
    { char: '🥄', keywords: ['勺子'] },
    { char: '🔪', keywords: ['刀'] },
    { char: '🏺', keywords: ['陶罐'] }
  ],
  activity: [
    { char: '⚽', keywords: ['足球'] },
    { char: '🏀', keywords: ['篮球'] },
    { char: '🏈', keywords: ['橄榄球'] },
    { char: '⚾', keywords: ['棒球'] },
    { char: '🥎', keywords: ['垒球'] },
    { char: '🎾', keywords: ['网球'] },
    { char: '🏐', keywords: ['排球'] },
    { char: '🏉', keywords: ['橄榄球'] },
    { char: '🥏', keywords: ['飞盘'] },
    { char: '🎱', keywords: ['台球'] },
    { char: '🪀', keywords: ['悠悠球'] },
    { char: '🏓', keywords: ['乒乓球'] },
    { char: '🏸', keywords: ['羽毛球'] },
    { char: '🏒', keywords: ['冰球'] },
    { char: '🏑', keywords: ['曲棍球'] },
    { char: '🥍', keywords: ['长曲棍球'] },
    { char: '🏏', keywords: ['板球'] },
    { char: '🥅', keywords: ['球门'] },
    { char: '⛳', keywords: ['高尔夫'] },
    { char: '🪁', keywords: ['风筝'] },
    { char: '🏹', keywords: ['射箭'] },
    { char: '🎣', keywords: ['钓鱼'] },
    { char: '🤿', keywords: ['潜水'] },
    { char: '🥊', keywords: ['拳击'] },
    { char: '🥋', keywords: ['武术'] },
    { char: '🎽', keywords: ['运动服'] },
    { char: '🛹', keywords: ['滑板'] },
    { char: '🛼', keywords: ['旱冰鞋'] },
    { char: '🛷', keywords: ['雪橇'] },
    { char: '⛸️', keywords: ['滑冰'] },
    { char: '🥌', keywords: ['冰壶'] },
    { char: '🎿', keywords: ['滑雪'] },
    { char: '⛷️', keywords: ['滑雪'] },
    { char: '🏂', keywords: ['滑雪板'] },
    { char: '🏋️', keywords: ['举重'] },
    { char: '🤼', keywords: ['摔跤'] },
    { char: '🤸', keywords: ['体操'] },
    { char: '⛹️', keywords: ['篮球'] },
    { char: '🤺', keywords: ['击剑'] },
    { char: '🤾', keywords: ['手球'] },
    { char: '🏌️', keywords: ['高尔夫'] },
    { char: '🏇', keywords: ['赛马'] },
    { char: '🧘', keywords: ['瑜伽'] },
    { char: '🏄', keywords: ['冲浪'] },
    { char: '🏊', keywords: ['游泳'] },
    { char: '🤽', keywords: ['水球'] },
    { char: '🚣', keywords: ['划船'] },
    { char: '🧗', keywords: ['攀岩'] },
    { char: '🚵', keywords: ['山地车'] },
    { char: '🚴', keywords: ['自行车'] },
    { char: '🏆', keywords: ['奖杯'] },
    { char: '🥇', keywords: ['金牌'] },
    { char: '🥈', keywords: ['银牌'] },
    { char: '🥉', keywords: ['铜牌'] },
    { char: '🏅', keywords: ['奖牌'] },
    { char: '🎖️', keywords: ['勋章'] },
    { char: '🏵️', keywords: ['玫瑰'] },
    { char: '🎗️', keywords: ['丝带'] },
    { char: '🎫', keywords: ['票'] },
    { char: '🎟️', keywords: ['票'] },
    { char: '🎪', keywords: ['马戏团'] },
    { char: '🤹', keywords: ['杂技'] },
    { char: '🎭', keywords: ['面具'] },
    { char: '🩰', keywords: ['舞鞋'] },
    { char: '🎨', keywords: ['艺术'] },
    { char: '🎬', keywords: ['电影'] },
    { char: '🎤', keywords: ['麦克风'] },
    { char: '🎧', keywords: ['耳机'] },
    { char: '🎼', keywords: ['乐谱'] },
    { char: '🎹', keywords: ['钢琴'] },
    { char: '🥁', keywords: ['鼓'] },
    { char: '🎷', keywords: ['萨克斯'] },
    { char: '🎺', keywords: ['小号'] },
    { char: '🎸', keywords: ['吉他'] },
    { char: '🪕', keywords: ['班卓琴'] },
    { char: '🎻', keywords: ['小提琴'] },
    { char: '🪈', keywords: ['长笛'] },
    { char: '🎮', keywords: ['游戏'] },
    { char: '🎰', keywords: ['老虎机'] },
    { char: '🎲', keywords: ['骰子'] },
    { char: '🧩', keywords: ['拼图'] },
    { char: '🧸', keywords: ['泰迪熊'] },
    { char: '♠️', keywords: ['黑桃'] },
    { char: '♥️', keywords: ['红心'] },
    { char: '♦️', keywords: ['方块'] },
    { char: '♣️', keywords: ['梅花'] },
    { char: '♟️', keywords: ['棋子'] },
    { char: '🃏', keywords: ['小丑牌'] },
    { char: '🀄', keywords: ['麻将'] },
    { char: '🎴', keywords: ['花札'] },
    { char: '🎯', keywords: ['靶心'] },
    { char: '🎳', keywords: ['保龄球'] },
    { char: '🎱', keywords: ['台球'] }
  ],
  object: [
    { char: '❤️', keywords: ['爱心'] },
    { char: '🧡', keywords: ['橙色爱心'] },
    { char: '💛', keywords: ['黄色爱心'] },
    { char: '💚', keywords: ['绿色爱心'] },
    { char: '💙', keywords: ['蓝色爱心'] },
    { char: '💜', keywords: ['紫色爱心'] },
    { char: '🖤', keywords: ['黑色爱心'] },
    { char: '🤍', keywords: ['白色爱心'] },
    { char: '🤎', keywords: ['棕色爱心'] },
    { char: '💔', keywords: ['碎心'] },
    { char: '❣️', keywords: ['感叹心'] },
    { char: '💕', keywords: ['双心'] },
    { char: '💞', keywords: ['爱心圈'] },
    { char: '💓', keywords: ['跳动的爱心'] },
    { char: '💗', keywords: ['成长的心'] },
    { char: '💖', keywords: ['闪耀的心'] },
    { char: '💘', keywords: ['心箭'] },
    { char: '💝', keywords: ['礼物心'] },
    { char: '💟', keywords: ['爱心'] },
    { char: '☮️', keywords: ['和平'] },
    { char: '✝️', keywords: ['十字架'] },
    { char: '☪️', keywords: ['星月'] },
    { char: '🕉️', keywords: ['唵'] },
    { char: '☸️', keywords: ['法轮'] },
    { char: '✡️', keywords: ['六芒星'] },
    { char: '🔯', keywords: ['大卫之星'] },
    { char: '🕎', keywords: ['烛台'] },
    { char: '☯️', keywords: ['阴阳'] },
    { char: '☦️', keywords: ['东正教'] },
    { char: '🛐', keywords: ['祈祷'] },
    { char: '⛎', keywords: ['星座'] },
    { char: '♈', keywords: ['白羊座'] },
    { char: '♉', keywords: ['金牛座'] },
    { char: '♊', keywords: ['双子座'] },
    { char: '♋', keywords: ['巨蟹座'] },
    { char: '♌', keywords: ['狮子座'] },
    { char: '♍', keywords: ['处女座'] },
    { char: '♎', keywords: ['天秤座'] },
    { char: '♏', keywords: ['天蝎座'] },
    { char: '♐', keywords: ['射手座'] },
    { char: '♑', keywords: ['摩羯座'] },
    { char: '♒', keywords: ['水瓶座'] },
    { char: '♓', keywords: ['双鱼座'] },
    { char: '🆔', keywords: ['ID'] },
    { char: '⚛️', keywords: ['原子'] },
    { char: '🉑', keywords: ['可'] },
    { char: '☢️', keywords: ['辐射'] },
    { char: '☣️', keywords: ['生物危害'] },
    { char: '📴', keywords: ['手机关闭'] },
    { char: '📳', keywords: ['手机震动'] },
    { char: '🈶', keywords: ['有'] },
    { char: '🈚', keywords: ['无'] },
    { char: '🈸', keywords: ['申请'] },
    { char: '🈺', keywords: ['营业'] },
    { char: '🈷️', keywords: ['月'] },
    { char: '🈶', keywords: ['有'] },
    { char: '✴️', keywords: ['光芒'] },
    { char: '❇️', keywords: ['火花'] },
    { char: '❎', keywords: ['X'] },
    { char: '✳️', keywords: ['八芒星'] },
    { char: '❌', keywords: ['X'] },
    { char: '❎', keywords: ['X'] },
    { char: '➕', keywords: ['加号'] },
    { char: '➖', keywords: ['减号'] },
    { char: '➗', keywords: ['除号'] },
    { char: '➰', keywords: ['丝带'] },
    { char: '➿', keywords: ['丝带'] },
    { char: '〽️', keywords: ['波浪'] },
    { char: '✳️', keywords: ['八芒星'] },
    { char: '✴️', keywords: ['光芒'] },
    { char: '❇️', keywords: ['火花'] },
    { char: '‼️', keywords: ['双感叹号'] },
    { char: '⁉️', keywords: ['感叹问号'] },
    { char: '❓', keywords: ['问号'] },
    { char: '❔', keywords: ['白问号'] },
    { char: '❕', keywords: ['白感叹号'] },
    { char: '❗', keywords: ['感叹号'] },
    { char: '〰️', keywords: ['波浪'] },
    { char: '©️', keywords: ['版权'] },
    { char: '®️', keywords: ['注册'] },
    { char: '™️', keywords: ['商标'] },
    { char: '#️⃣', keywords: ['井号'] },
    { char: '*️⃣', keywords: ['星号'] },
    { char: '0️⃣', keywords: ['0'] },
    { char: '1️⃣', keywords: ['1'] },
    { char: '2️⃣', keywords: ['2'] },
    { char: '3️⃣', keywords: ['3'] },
    { char: '4️⃣', keywords: ['4'] },
    { char: '5️⃣', keywords: ['5'] },
    { char: '6️⃣', keywords: ['6'] },
    { char: '7️⃣', keywords: ['7'] },
    { char: '8️⃣', keywords: ['8'] },
    { char: '9️⃣', keywords: ['9'] },
    { char: '🔟', keywords: ['10'] },
    { char: '🔠', keywords: ['ABCD'] },
    { char: '🔡', keywords: ['abcd'] },
    { char: '🔢', keywords: ['123'] },
    { char: '🔣', keywords: ['符号'] },
    { char: '🔤', keywords: ['ABCD'] },
    { char: '🅰️', keywords: ['A'] },
    { char: '🆎', keywords: ['AB'] },
    { char: '🅱️', keywords: ['B'] },
    { char: '🆑', keywords: ['CL'] },
    { char: '🆒', keywords: ['COOL'] },
    { char: '🆓', keywords: ['FREE'] },
    { char: 'ℹ️', keywords: ['INFO'] },
    { char: '🆔', keywords: ['ID'] },
    { char: 'Ⓜ️', keywords: ['M'] },
    { char: '🆕', keywords: ['NEW'] },
    { char: '🆖', keywords: ['NG'] },
    { char: '🅾️', keywords: ['O'] },
    { char: '🆗', keywords: ['OK'] },
    { char: '🅿️', keywords: ['P'] },
    { char: '🆘', keywords: ['SOS'] },
    { char: '🆔', keywords: ['ID'] },
    { char: '🆙', keywords: ['UP'] },
    { char: '🆚', keywords: ['VS'] },
    { char: '🈁', keywords: ['这里'] },
    { char: '🈂️', keywords: ['服务'] },
    { char: '🔴', keywords: ['红圈'] },
    { char: '🟠', keywords: ['橙圈'] },
    { char: '🟡', keywords: ['黄圈'] },
    { char: '🟢', keywords: ['绿圈'] },
    { char: '🔵', keywords: ['蓝圈'] },
    { char: '🟣', keywords: ['紫圈'] },
    { char: '🟤', keywords: ['棕圈'] },
    { char: '⚫', keywords: ['黑圈'] },
    { char: '⚪', keywords: ['白圈'] },
    { char: '🟥', keywords: ['红方块'] },
    { char: '🟧', keywords: ['橙方块'] },
    { char: '🟨', keywords: ['黄方块'] },
    { char: '🟩', keywords: ['绿方块'] },
    { char: '🟦', keywords: ['蓝方块'] },
    { char: '🟪', keywords: ['紫方块'] },
    { char: '🟫', keywords: ['棕方块'] },
    { char: '⬛', keywords: ['黑方块'] },
    { char: '⬜', keywords: ['白方块'] },
    { char: '◼️', keywords: ['黑方块'] },
    { char: '◻️', keywords: ['白方块'] },
    { char: '◾', keywords: ['黑方块'] },
    { char: '◽', keywords: ['白方块'] },
    { char: '▪️', keywords: ['黑小方块'] },
    { char: '▫️', keywords: ['白小方块'] },
    { char: '🔶', keywords: ['大橙菱形'] },
    { char: '🔷', keywords: ['大蓝菱形'] },
    { char: '🔸', keywords: ['小橙菱形'] },
    { char: '🔹', keywords: ['小蓝菱形'] },
    { char: '🔺', keywords: ['红三角'] },
    { char: '🔻', keywords: ['倒红三角'] },
    { char: '💠', keywords: ['菱形'] },
    { char: '🔘', keywords: ['按钮'] },
    { char: '🔳', keywords: ['白按钮'] },
    { char: '🔲', keywords: ['黑按钮'] }
  ]
}

// 当前标签的表情
const currentEmojis = computed(() => {
  return emojiData[activeTab.value] || []
})

// 选择表情
const selectEmoji = (char) => {
  emit('select', char)
}
</script>

<style scoped lang="scss">
.emoji-picker {
  width: 320px;
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  border-radius: 12px;
  box-shadow: var(--dt-shadow-lg);
  overflow: hidden;
  position: absolute;
  bottom: 100%;
  left: 0;
  margin-bottom: 10px;
  z-index: var(--dt-z-dropdown);
  animation: slideUp 0.2s ease-out;

  @keyframes slideUp {
    from { opacity: 0; transform: translateY(10px); }
    to { opacity: 1; transform: translateY(0); }
  }

  .emoji-tabs {
    display: flex;
    background-color: var(--dt-bg-body);
    padding: 6px;
    gap: 4px;

    .emoji-tab {
      flex: 1;
      text-align: center;
      padding: 6px 0;
      cursor: pointer;
      border-radius: 8px;
      font-size: 18px;
      transition: all 0.2s;
      display: flex;
      align-items: center;
      justify-content: center;

      &:hover {
        background: rgba(0, 0, 0, 0.05);
      }

      &.active {
        background: #fff;
        box-shadow: 0 1px 3px rgba(0,0,0,0.1);
        color: var(--dt-brand-color);
      }
    }
  }

  .emoji-grid {
    height: 240px;
    overflow-y: auto;
    padding: 12px;
    display: grid;
    grid-template-columns: repeat(8, 1fr);
    gap: 8px;
    
    &::-webkit-scrollbar { width: 4px; }
    &::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 10px; }

    .emoji-item {
      aspect-ratio: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 22px;
      cursor: pointer;
      border-radius: 8px;
      transition: all 0.15s cubic-bezier(0.4, 0, 0.2, 1);
      user-select: none;

      &:hover {
        background: var(--dt-bg-session-hover);
        transform: scale(1.25);
        z-index: 10;
      }
      
      &:active {
        transform: scale(0.9);
      }
    }
  }
}

.dark .emoji-picker {
  .emoji-tabs {
    background-color: #1e293b;
    .emoji-tab.active { background-color: #334155; }
  }
}
</style>
