<template>
  <div v-if="visible" class="emoji-picker" @click.stop>
    <!-- 搜索栏 -->
    <div class="emoji-search">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索表情..."
        :prefix-icon="Search"
        size="small"
        clearable
        @input="handleSearch"
      />
    </div>

    <!-- 最近使用 -->
    <div v-if="!searchKeyword && recentEmojis.length > 0" class="emoji-section">
      <div class="section-title">最近使用</div>
      <div class="emoji-grid compact">
        <div
          v-for="emoji in recentEmojis"
          :key="emoji.char"
          class="emoji-item"
          :title="emoji.keywords?.[0] || ''"
          @click="selectEmoji(emoji.char)"
        >
          {{ emoji.char }}
        </div>
      </div>
    </div>

    <!-- 分类标签 -->
    <div v-if="!searchKeyword" class="emoji-tabs">
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

    <!-- 表情网格 -->
    <div class="emoji-grid" :class="{ 'full-height': searchKeyword || !recentEmojis.length }">
      <div
        v-for="emoji in currentEmojis"
        :key="emoji.char"
        class="emoji-item"
        :title="emoji.keywords?.[0] || ''"
        @click="selectEmoji(emoji.char)"
      >
        {{ emoji.char }}
      </div>

      <!-- 无搜索结果 -->
      <div v-if="currentEmojis.length === 0" class="no-results">
        <span class="material-icons-outlined">search_off</span>
        <span>未找到相关表情</span>
      </div>
    </div>

    <!-- 删除按钮（当有选中表情时显示） -->
    <div v-if="false" class="emoji-delete">
      <el-button circle size="small" @click="handleDelete">
        <el-icon><Delete /></el-icon>
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { Search, Delete } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['select'])

const activeTab = ref('smile')
const searchKeyword = ref('')

// 本地存储的最近表情
const STORAGE_KEY = 'im_recent_emojis'
const MAX_RECENT = 20

const recentEmojis = ref([])

// 从本地存储加载最近表情
const loadRecentEmojis = () => {
  try {
    const stored = localStorage.getItem(STORAGE_KEY)
    if (stored) {
      const parsed = JSON.parse(stored)
      recentEmojis.value = parsed || []
    }
  } catch (e) {
    console.error('加载最近表情失败', e)
  }
}

// 保存最近表情到本地存储
const saveRecentEmojis = (emojis) => {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(emojis))
  } catch (e) {
    console.error('保存最近表情失败', e)
  }
}

// 添加表情到最近使用
const addToRecent = (char) => {
  const emojiData = getAllEmojis()
  const emoji = emojiData.find(e => e.char === char)

  if (!emoji) return

  // 移除已存在的
  recentEmojis.value = recentEmojis.value.filter(e => e.char !== char)

  // 添加到开头
  recentEmojis.value.unshift(emoji)

  // 限制数量
  if (recentEmojis.value.length > MAX_RECENT) {
    recentEmojis.value = recentEmojis.value.slice(0, MAX_RECENT)
  }

  saveRecentEmojis(recentEmojis.value)
}

// 获取所有表情数据
const getAllEmojis = () => {
  const all = []
  Object.values(emojiData).forEach(arr => all.push(...arr))
  return all
}

// 表情分类标签
const tabs = [
  { type: 'smile', label: '😀' },
  { type: 'hand', label: '👋' },
  { type: 'animal', label: '🐶' },
  { type: 'food', label: '🍎' },
  { type: 'activity', label: '⚽' },
  { type: 'object', label: '❤️' }
]

// 表情数据
const emojiData = {
  smile: [
    { char: '😀', keywords: ['开心', '笑脸', '哈哈'] },
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
    { char: '😍', keywords: ['爱', '喜欢'] },
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
    { char: '🤑', keywords: ['钱', '发财'] },
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
    { char: '😎', keywords: ['墨镜', '酷'] },
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
    { char: '🙈', keywords: ['不看', '非礼勿视'] },
    { char: '🙉', keywords: ['不听', '非礼勿听'] },
    { char: '🙊', keywords: ['不说', '非礼勿言'] },
    { char: '👋', keywords: ['挥手', '再见'] },
    { char: '🤚', keywords: ['举手'] },
    { char: '🖐️', keywords: ['手掌'] },
    { char: '✋', keywords: ['手掌'] },
    { char: '🖖', keywords: ['瓦肯举手礼'] },
    { char: '👌', keywords: ['OK', '好的'] },
    { char: '🤌', keywords: ['捏手指'] },
    { char: '🤏', keywords: ['捏手指'] },
    { char: '✌️', keywords: ['V字', '胜利'] },
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
    { char: '👍', keywords: ['赞', '棒', '点赞'] },
    { char: '👎', keywords: ['踩', '差'] },
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
    { char: '👍', keywords: ['赞', '点赞'] },
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
    { char: '💪', keywords: ['肌肉', '强'] },
    { char: '🦾', keywords: ['机械臂'] },
    { char: '🦿', keywords: ['机械腿'] },
    { char: '🦵', keywords: ['腿'] },
    { char: '🦶', keywords: ['脚'] },
    { char: '👂', keywords: ['耳朵'] },
    { char: '🦻', keywords: ['助听器'] },
    { char: '👃', keywords: ['鼻子'] }
  ],
  animal: [
    { char: '🐶', keywords: ['狗', '汪汪'] },
    { char: '🐱', keywords: ['猫', '喵'] },
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
    { char: '🍀', keywords: ['四叶草', '幸运'] }
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
    { char: '🧊', keywords: ['冰块'] }
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
    { char: '🎗️', keywords: ['丝带'] },
    { char: '🎫', keywords: ['票'] },
    { char: '🎟️', keywords: ['票'] },
    { char: '🎪', keywords: ['马戏团'] },
    { char: '🤹', keywords: ['杂技'] },
    { char: '🎭', keywords: ['面具'] },
    { char: '🩰', keywords: ['舞鞋'] },
    { char: '🎨', keywords: ['艺术'] },
    { char: '🎬', keywords: ['电影'] },
    { char: '🎤', keywords: ['麦克风', '唱歌'] },
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
    { char: '🎲', keywords: ['骰子'] }
  ],
  object: [
    { char: '❤️', keywords: ['爱心', '爱'] },
    { char: '🧡', keywords: ['橙色爱心'] },
    { char: '💛', keywords: ['黄色爱心'] },
    { char: '💚', keywords: ['绿色爱心'] },
    { char: '💙', keywords: ['蓝色爱心'] },
    { char: '💜', keywords: ['紫色爱心'] },
    { char: '🖤', keywords: ['黑色爱心'] },
    { char: '🤍', keywords: ['白色爱心'] },
    { char: '🤎', keywords: ['棕色爱心'] },
    { char: '💔', keywords: ['碎心', '分手'] },
    { char: '❣️', keywords: ['感叹心'] },
    { char: '💕', keywords: ['双心'] },
    { char: '💞', keywords: ['爱心圈'] },
    { char: '💓', keywords: ['跳动的爱心'] },
    { char: '💗', keywords: ['成长的心'] },
    { char: '💖', keywords: ['闪耀的心'] },
    { char: '💘', keywords: ['心箭'] },
    { char: '💝', keywords: ['礼物心'] },
    { char: '💟', keywords: ['爱心'] },
    { char: '💯', keywords: ['满分', '100分'] },
    { char: '💢', keywords: ['感叹'] },
    { char: '💥', keywords: ['爆炸'] },
    { char: '💫', keywords: ['闪耀'] },
    { char: '💦', keywords: ['水滴'] },
    { char: '💨', keywords: ['烟雾'] },
    { char: '🕳️', keywords: ['洞'] },
    { char: '💣', keywords: ['炸弹'] },
    { char: '💬', keywords: ['对话'] },
    { char: '🗨️', keywords: ['对话'] },
    { char: '🗯', keywords: ['说话'] },
    { char: '💭', keywords: ['思考'] },
    { char: '💤', keywords: ['睡眠'] },
    { char: '👁️', keywords: ['眼睛'] },
    { char: '👂', keywords: ['耳朵'] },
    { char: '👃', keywords: ['鼻子'] },
    { char: '🧠', keywords: ['大脑'] },
    { char: '🦷', keywords: ['牙齿'] },
    { char: '🦴', keywords: ['骨头'] },
    { char: '👀', keywords: ['眼睛'] },
    { char: '👄', keywords: ['手'] },
    { char: '🗣️', keywords: ['嘴'] },
    { char: '👤', keywords: ['人'] },
    { char: '👥', keywords: ['人'] },
    { char: '🐵', keywords: ['猴子'] },
    { char: '🐶', keywords: ['狗'] },
    { char: '🐱', keywords: ['猫'] },
    { char: '🐭', keywords: ['老鼠'] },
    { char: '🐹', keywords: ['仓鼠'] },
    { char: '🐰', keywords: ['兔子'] },
    { char: '🦊', keywords: ['狐狸'] },
    { char: '🐻', keywords: ['熊'] },
    { char: '🐼', keywords: ['熊猫'] },
    { char: '🐯', keywords: ['老虎'] },
    { char: '🦁', keywords: ['狮子'] },
    { char: '💔', keywords: ['心碎'] }
  ]
}

// 当前标签的表情
const currentEmojis = computed(() => {
  if (searchKeyword.value) {
    // 搜索模式：搜索所有表情
    const keyword = searchKeyword.value.toLowerCase().trim()
    if (!keyword) return emojiData.smile

    const allEmojis = getAllEmojis()
    return allEmojis.filter(emoji => {
      const char = emoji.char
      const keywords = emoji.keywords || []
      return char.includes(keyword) || keywords.some(k => k.includes(keyword))
    })
  }
  return emojiData[activeTab.value] || []
})

// 选择表情
const selectEmoji = (char) => {
  addToRecent(char)
  emit('select', char)
}

// 搜索处理
const handleSearch = () => {
  // 搜索逻辑由 computed 自动处理
}

// 初始化
onMounted(() => {
  loadRecentEmojis()
})

// 监听显示状态，重置搜索
watch(() => props.visible, (val) => {
  if (!val) {
    searchKeyword.value = ''
    activeTab.value = 'smile'
  }
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.emoji-picker {
  width: 360px;
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  border-radius: 12px;
  box-shadow: var(--dt-shadow-lg);
  overflow: hidden;
  position: absolute;
  bottom: calc(100% + 8px);
  left: 0;
  z-index: var(--dt-z-dropdown);
  animation: slideUp 0.25s cubic-bezier(0.4, 0, 0.2, 1);

  @keyframes slideUp {
    from {
      opacity: 0;
      transform: translateY(12px) scale(0.95);
    }
    to {
      opacity: 1;
      transform: translateY(0) scale(1);
    }
  }

  .emoji-search {
    padding: 12px 12px 8px;
    background: var(--dt-bg-body);
    border-bottom: 1px solid var(--dt-border-light);
  }

  .emoji-section {
    .section-title {
      font-size: 12px;
      color: var(--dt-text-tertiary);
      padding: 8px 12px 4px;
    }
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
        background: rgba(0, 0, 0, 0.04);
      }

      &.active {
        background: #fff;
        box-shadow: 0 1px 3px rgba(0,0,0,0.08);
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
    gap: 6px;

    &.full-height {
      height: 300px;
    }

    &.compact {
      height: 80px;
      grid-template-columns: repeat(10, 1fr);
    }

    &::-webkit-scrollbar {
      width: 4px;
    }

    &::-webkit-scrollbar-track {
      background: transparent;
    }

    &::-webkit-scrollbar-thumb {
      background: rgba(0, 0, 0, 0.1);
      border-radius: 10px;

      &:hover {
        background: rgba(0, 0, 0, 0.2);
      }
    }

    .emoji-item {
      aspect-ratio: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 22px;
      cursor: pointer;
      border-radius: 6px;
      transition: all 0.12s cubic-bezier(0.4, 0, 0.2, 1);
      user-select: none;

      &:hover {
        background: var(--dt-bg-hover);
        transform: scale(1.2);
        z-index: 10;
      }

      &:active {
        transform: scale(0.95);
      }
    }
  }

  .no-results {
    grid-column: 1 / -1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 8px;
    color: var(--dt-text-quaternary);
    height: 100%;
    font-size: 13px;

    .material-icons-outlined {
      font-size: 32px;
      opacity: 0.5;
    }
  }
}

// 暗色模式
.dark .emoji-picker {
  .emoji-tabs {
    background-color: var(--dt-bg-hover-dark);

    .emoji-tab {
      &.active {
        background: var(--dt-bg-selected-dark);
        color: var(--dt-brand-color);
      }

      &:hover {
        background: rgba(255, 255, 255, 0.06);
      }
    }
  }

  .emoji-grid .emoji-item {
    &:hover {
      background: var(--dt-bg-hover-dark);
    }
  }

  .emoji-search {
    background: var(--dt-bg-hover-dark);
    border-color: var(--dt-border-dark);
  }

  .section-title {
    color: var(--dt-text-tertiary-dark);
  }
}
</style>
