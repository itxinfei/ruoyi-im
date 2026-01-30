/**
 * 拼音转换工具
 * 支持中文转拼音首字母和全拼，用于搜索功能
 */

/**
 * 简单的拼音首字母映射表
 * 仅包含常用汉字，用于首字母搜索
 */
const PINYIN_FIRST_LETTER = {
  // 这里只列出一部分示例，实际应用中可以使用完整的拼音库
  // 或者使用第三方库如 pinyin-pro
  'a': '阿啊', 'b': '八巴拔吧白百班般半包宝保报贝本比必边便别变宾冰波伯博不步布部',
  'c': '才材采彩参餐残仓草策层曾叉查差产长常场唱车彻陈诚成承城吃充出除楚川传船窗床春次从粗促催村存错',
  'd': '达大代带待单但淡当党导到道的得德登等低底地点电店调定东冬动都读度短对多朵',
  'e': '阿恶恩儿耳而二',
  'f': '法发反范方房防放非飞费份分风封夫服福府父复付副富',
  'g': '改敢感刚钢高搞告歌格个给根跟更工公功共构姑古古骨固故顾瓜刮关观官管光广规归贵国果过',
  'h': '哈孩海含汉好合和何河核黑红后呼乎胡湖护花华化画话欢换黄回会婚活火获或',
  'j': '机基极急集计记纪技季家加假价间简建健江将交角脚叫教接节结姐解介界金今进近精景静究九久酒旧就局居举具句觉决军',
  'k': '卡开看康考靠科可课肯肯空口苦库快宽款况',
  'l': '拉来蓝老乐李里理力立利连良亮了列林灵领流留六龙楼路录陆伦罗绿律乱略论',
  'm': '马买卖满慢忙毛美门们米密面民明名命模莫母木目',
  'n': '那拿哪内那男南难能尼你年念鸟您牛农',
  'o': '哦欧偶',
  'p': '排派盘判跑配朋皮片平评瓶坡破普',
  'q': '七期其奇起气汽千前钱强桥巧切且亲轻青清情庆秋求球区曲取去全缺确群',
  'r': '然让热人仁任日容肉如入若弱',
  's': '三色森沙山善商上少绍社设申身深什生声胜师施十时事手守首受书术树数双水税睡思四送诉算随岁所索',
  't': '他它她台太态谈特提题体天填条铁听停通同统头图土团推退',
  'w': '瓦外完玩晚王万往忘望威伟为位文闻问翁我无五物务误',
  'x': '西希系息喜细下夏先仙显示香乡相向项小笑校些谢心新信信行星形修须许续学雪血寻训讯',
  'y': '压呀牙亚烟严研言颜眼演阳养要也业叶夜一医衣依疑已以用由游友有又右于鱼雨语玉元员原远院月越云运',
  'z': '杂在早造则增展站张章招找照者这真正整政之支知直值职指止至志制治中中钟终种重众周州朱主住注祝专转赚准桌资子字自总走租组最左作做坐座'
}

/**
 * 将字符转换为拼音首字母
 * @param {string} char - 单个汉字
 * @returns {string} 拼音首字母
 */
function charToFirstLetter(char) {
  // 数字和字母直接返回
  if (/[a-zA-Z0-9]/.test(char)) {
    return char.toLowerCase()
  }

  // 遍历首字母映射表
  for (const letter in PINYIN_FIRST_LETTER) {
    if (PINYIN_FIRST_LETTER[letter].includes(char)) {
      return letter
    }
  }

  return '#'
}

/**
 * 获取字符串的拼音首字母
 * @param {string} str - 输入字符串
 * @returns {string} 拼音首字母字符串
 */
export function getFirstLetter(str) {
  if (!str) return ''
  return Array.from(str)
    .map(char => charToFirstLetter(char))
    .join('')
    .toUpperCase()
}

/**
 * 获取字符串的匹配标记（用于搜索高亮）
 * @param {string} text - 原始文本
 * @param {string} keyword - 搜索关键词
 * @returns {Object} { matched, pinyin }
 */
export function matchPinyin(text, keyword) {
  if (!text || !keyword) return { matched: false, pinyin: '' }

  const textLower = text.toLowerCase()
  const keywordLower = keyword.toLowerCase()
  const pinyin = getFirstLetter(text)

  // 直接匹配
  if (textLower.includes(keywordLower)) {
    return { matched: true, pinyin }
  }

  // 拼音首字母匹配
  if (pinyin.toLowerCase().includes(keywordLower)) {
    return { matched: true, pinyin }
  }

  return { matched: false, pinyin }
}

/**
 * 批量获取字符串的拼音首字母
 * @param {Array<string>} strings - 字符串数组
 * @returns {Array<string>} 拼音首字母数组
 */
export function batchGetFirstLetter(strings) {
  if (!Array.isArray(strings)) return []
  return strings.map(str => getFirstLetter(str))
}

export default {
  getFirstLetter,
  matchPinyin,
  batchGetFirstLetter
}
