import type { Language } from '@/model/language.ts'
import { defineStore } from 'pinia'

export const useLanguageDataStore = defineStore('language-data', {
  state: () => {
    return {
      languages: [] as Language[],
    }
  },
  actions: {
    loadData() {
      this.languages = testData()
    },
  }
})

function testData(): Language[] {
  return [
    {
      name: "Afrikaans",
      alpha2: "af"
    },
    {
      name: "Aghem",
      alpha2: "agq"
    },
    {
      name: "Akan",
      alpha2: "ak"
    },
    {
      name: "Albanian",
      alpha2: "sq"
    },
    {
      name: "Amharic",
      alpha2: "am"
    },
    {
      name: "Arabic",
      alpha2: "ar"
    },
    {
      name: "Armenian",
      alpha2: "hy"
    },
    {
      name: "Assamese",
      alpha2: "as"
    },
    {
      name: "Asturian",
      alpha2: "ast"
    },
    {
      name: "Asu",
      alpha2: "asa"
    },
    {
      name: "Azerbaijani",
      alpha2: "az"
    },
    {
      name: "Bafia",
      alpha2: "ksf"
    },
    {
      name: "Bambara",
      alpha2: "bm"
    },
    {
      name: "Bangla",
      alpha2: "bn"
    },
    {
      name: "Basaa",
      alpha2: "bas"
    },
    {
      name: "Basque",
      alpha2: "eu"
    },
    {
      name: "Belarusian",
      alpha2: "be"
    },
    {
      name: "Bemba",
      alpha2: "bem"
    },
    {
      name: "Bena",
      alpha2: "bez"
    },
    {
      name: "Bodo",
      alpha2: "brx"
    },
    {
      name: "Bosnian",
      alpha2: "bs"
    },
    {
      name: "Breton",
      alpha2: "br"
    },
    {
      name: "Bulgarian",
      alpha2: "bg"
    },
    {
      name: "Burmese",
      alpha2: "my"
    },
    {
      name: "Cantonese",
      alpha2: "yue"
    },
    {
      name: "Catalan",
      alpha2: "ca"
    },
    {
      name: "CentralAtlasTamazight",
      alpha2: "tzm"
    },
    {
      name: "CentralKurdish",
      alpha2: "ckb"
    },
    {
      name: "Chakma",
      alpha2: "ccp"
    },
    {
      name: "Chechen",
      alpha2: "ce"
    },
    {
      name: "Cherokee",
      alpha2: "chr"
    },
    {
      name: "Chiga",
      alpha2: "cgg"
    },
    {
      name: "Chinese",
      alpha2: "zh"
    },
    {
      name: "Colognian",
      alpha2: "ksh"
    },
    {
      name: "Cornish",
      alpha2: "kw"
    },
    {
      name: "Croatian",
      alpha2: "hr"
    },
    {
      name: "Czech",
      alpha2: "cs"
    },
    {
      name: "Danish",
      alpha2: "da"
    },
    {
      name: "Duala",
      alpha2: "dua"
    },
    {
      name: "Dutch",
      alpha2: "nl"
    },
    {
      name: "Dzongkha",
      alpha2: "dz"
    },
    {
      name: "Embu",
      alpha2: "ebu"
    },
    {
      name: "English",
      alpha2: "en"
    },
    {
      name: "Esperanto",
      alpha2: "eo"
    },
    {
      name: "Estonian",
      alpha2: "et"
    },
    {
      name: "Ewe",
      alpha2: "ee"
    },
    {
      name: "Ewondo",
      alpha2: "ewo"
    },
    {
      name: "Faroese",
      alpha2: "fo"
    },
    {
      name: "Filipino",
      alpha2: "fil"
    },
    {
      name: "Finnish",
      alpha2: "fi"
    },
    {
      name: "French",
      alpha2: "fr"
    },
    {
      name: "Friulian",
      alpha2: "fur"
    },
    {
      name: "Fulah",
      alpha2: "ff"
    },
    {
      name: "Galician",
      alpha2: "gl"
    },
    {
      name: "Ganda",
      alpha2: "lg"
    },
    {
      name: "Georgian",
      alpha2: "ka"
    },
    {
      name: "German",
      alpha2: "de"
    },
    {
      name: "Greek",
      alpha2: "el"
    },
    {
      name: "Gujarati",
      alpha2: "gu"
    },
    {
      name: "Gusii",
      alpha2: "guz"
    },
    {
      name: "Hausa",
      alpha2: "ha"
    },
    {
      name: "Hawaiian",
      alpha2: "haw"
    },
    {
      name: "Hebrew",
      alpha2: "he"
    },
    {
      name: "Hindi",
      alpha2: "hi"
    },
    {
      name: "Hungarian",
      alpha2: "hu"
    },
    {
      name: "Icelandic",
      alpha2: "is"
    },
    {
      name: "Igbo",
      alpha2: "ig"
    },
    {
      name: "InariSami",
      alpha2: "smn"
    },
    {
      name: "Indonesian",
      alpha2: "id"
    },
    {
      name: "Irish",
      alpha2: "ga"
    },
    {
      name: "Italian",
      alpha2: "it"
    },
    {
      name: "Japanese",
      alpha2: "ja"
    },
    {
      name: "Jola-Fonyi",
      alpha2: "dyo"
    },
    {
      name: "Kabuverdianu",
      alpha2: "kea"
    },
    {
      name: "Kabyle",
      alpha2: "kab"
    },
    {
      name: "Kako",
      alpha2: "kkj"
    },
    {
      name: "Kalaallisut",
      alpha2: "kl"
    },
    {
      name: "Kalenjin",
      alpha2: "kln"
    },
    {
      name: "Kamba",
      alpha2: "kam"
    },
    {
      name: "Kannada",
      alpha2: "kn"
    },
    {
      name: "Kashmiri",
      alpha2: "ks"
    },
    {
      name: "Kazakh",
      alpha2: "kk"
    },
    {
      name: "Khmer",
      alpha2: "km"
    },
    {
      name: "Kikuyu",
      alpha2: "ki"
    },
    {
      name: "Kinyarwanda",
      alpha2: "rw"
    },
    {
      name: "Konkani",
      alpha2: "kok"
    },
    {
      name: "Korean",
      alpha2: "ko"
    },
    {
      name: "KoyraChiini",
      alpha2: "khq"
    },
    {
      name: "KoyraboroSenni",
      alpha2: "ses"
    },
    {
      name: "Kwasio",
      alpha2: "nmg"
    },
    {
      name: "Kyrgyz",
      alpha2: "ky"
    },
    {
      name: "Lakota",
      alpha2: "lkt"
    },
    {
      name: "Langi",
      alpha2: "lag"
    },
    {
      name: "Lao",
      alpha2: "lo"
    },
    {
      name: "Latvian",
      alpha2: "lv"
    },
    {
      name: "Lingala",
      alpha2: "ln"
    },
    {
      name: "Lithuanian",
      alpha2: "lt"
    },
    {
      name: "LowGerman",
      alpha2: "nds"
    },
    {
      name: "LowerSorbian",
      alpha2: "dsb"
    },
    {
      name: "Luba-Katanga",
      alpha2: "lu"
    },
    {
      name: "Luo",
      alpha2: "luo"
    },
    {
      name: "Luxembourgish",
      alpha2: "lb"
    },
    {
      name: "Luyia",
      alpha2: "luy"
    },
    {
      name: "Macedonian",
      alpha2: "mk"
    },
    {
      name: "Machame",
      alpha2: "jmc"
    },
    {
      name: "Makhuwa-Meetto",
      alpha2: "mgh"
    },
    {
      name: "Makonde",
      alpha2: "kde"
    },
    {
      name: "Malagasy",
      alpha2: "mg"
    },
    {
      name: "Malay",
      alpha2: "ms"
    },
    {
      name: "Malayalam",
      alpha2: "ml"
    },
    {
      name: "Maltese",
      alpha2: "mt"
    },
    {
      name: "Manx",
      alpha2: "gv"
    },
    {
      name: "Marathi",
      alpha2: "mr"
    },
    {
      name: "Masai",
      alpha2: "mas"
    },
    {
      name: "Mazanderani",
      alpha2: "mzn"
    },
    {
      name: "Meru",
      alpha2: "mer"
    },
    {
      name: "Metaʼ",
      alpha2: "mgo"
    },
    {
      name: "Mongolian",
      alpha2: "mn"
    },
    {
      name: "Morisyen",
      alpha2: "mfe"
    },
    {
      name: "Mundang",
      alpha2: "mua"
    },
    {
      name: "Nama",
      alpha2: "naq"
    },
    {
      name: "Nepali",
      alpha2: "ne"
    },
    {
      name: "Ngiemboon",
      alpha2: "nnh"
    },
    {
      name: "Ngomba",
      alpha2: "jgo"
    },
    {
      name: "NorthNdebele",
      alpha2: "nd"
    },
    {
      name: "NorthernLuri",
      alpha2: "lrc"
    },
    {
      name: "NorthernSami",
      alpha2: "se"
    },
    {
      name: "NorwegianBokmål",
      alpha2: "nb"
    },
    {
      name: "NorwegianNynorsk",
      alpha2: "nn"
    },
    {
      name: "Nuer",
      alpha2: "nus"
    },
    {
      name: "Nyankole",
      alpha2: "nyn"
    },
    {
      name: "Odia",
      alpha2: "or"
    },
    {
      name: "Oromo",
      alpha2: "om"
    },
    {
      name: "Ossetic",
      alpha2: "os"
    },
    {
      name: "Pashto",
      alpha2: "ps"
    },
    {
      name: "Persian",
      alpha2: "fa"
    },
    {
      name: "Polish",
      alpha2: "pl"
    },
    {
      name: "Portuguese",
      alpha2: "pt"
    },
    {
      name: "Punjabi",
      alpha2: "pa"
    },
    {
      name: "Quechua",
      alpha2: "qu"
    },
    {
      name: "Romanian",
      alpha2: "ro"
    },
    {
      name: "Romansh",
      alpha2: "rm"
    },
    {
      name: "Rombo",
      alpha2: "rof"
    },
    {
      name: "Rundi",
      alpha2: "rn"
    },
    {
      name: "Russian",
      alpha2: "ru"
    },
    {
      name: "Rwa",
      alpha2: "rwk"
    },
    {
      name: "Sakha",
      alpha2: "sah"
    },
    {
      name: "Samburu",
      alpha2: "saq"
    },
    {
      name: "Sango",
      alpha2: "sg"
    },
    {
      name: "Sangu",
      alpha2: "sbp"
    },
    {
      name: "ScottishGaelic",
      alpha2: "gd"
    },
    {
      name: "Sena",
      alpha2: "seh"
    },
    {
      name: "Serbian",
      alpha2: "sr"
    },
    {
      name: "Shambala",
      alpha2: "ksb"
    },
    {
      name: "Shona",
      alpha2: "sn"
    },
    {
      name: "SichuanYi",
      alpha2: "ii"
    },
    {
      name: "Sinhala",
      alpha2: "si"
    },
    {
      name: "Slovak",
      alpha2: "sk"
    },
    {
      name: "Slovenian",
      alpha2: "sl"
    },
    {
      name: "Soga",
      alpha2: "xog"
    },
    {
      name: "Somali",
      alpha2: "so"
    },
    {
      name: "Spanish",
      alpha2: "es"
    },
    {
      name: "StandardMoroccanTamazight",
      alpha2: "zgh"
    },
    {
      name: "Swahili",
      alpha2: "sw"
    },
    {
      name: "Swedish",
      alpha2: "sv"
    },
    {
      name: "SwissGerman",
      alpha2: "gsw"
    },
    {
      name: "Tachelhit",
      alpha2: "shi"
    },
    {
      name: "Taita",
      alpha2: "dav"
    },
    {
      name: "Tajik",
      alpha2: "tg"
    },
    {
      name: "Tamil",
      alpha2: "ta"
    },
    {
      name: "Tasawaq",
      alpha2: "twq"
    },
    {
      name: "Tatar",
      alpha2: "tt"
    },
    {
      name: "Telugu",
      alpha2: "te"
    },
    {
      name: "Teso",
      alpha2: "teo"
    },
    {
      name: "Thai",
      alpha2: "th"
    },
    {
      name: "Tibetan",
      alpha2: "bo"
    },
    {
      name: "Tigrinya",
      alpha2: "ti"
    },
    {
      name: "Tongan",
      alpha2: "to"
    },
    {
      name: "Turkish",
      alpha2: "tr"
    },
    {
      name: "Ukrainian",
      alpha2: "uk"
    },
    {
      name: "UpperSorbian",
      alpha2: "hsb"
    },
    {
      name: "Urdu",
      alpha2: "ur"
    },
    {
      name: "Uyghur",
      alpha2: "ug"
    },
    {
      name: "Uzbek",
      alpha2: "uz"
    },
    {
      name: "Vai",
      alpha2: "vai"
    },
    {
      name: "Vietnamese",
      alpha2: "vi"
    },
    {
      name: "Vunjo",
      alpha2: "vun"
    },
    {
      name: "Walser",
      alpha2: "wae"
    },
    {
      name: "Welsh",
      alpha2: "cy"
    },
    {
      name: "WesternFrisian",
      alpha2: "fy"
    },
    {
      name: "Wolof",
      alpha2: "wo"
    },
    {
      name: "Yangben",
      alpha2: "yav"
    },
    {
      name: "Yiddish",
      alpha2: "yi"
    },
    {
      name: "Yoruba",
      alpha2: "yo"
    },
    {
      name: "Zarma",
      alpha2: "dje"
    },
    {
      name: "Zulu",
      alpha2: "zu"
    }
  ]
}
