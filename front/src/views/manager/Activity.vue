<template>
  <div>
    <div class="search">
      <el-input placeholder="please enter title" style="width: 200px" v-model="name"></el-input>
      <el-button type="info" plain style="margin-left: 10px" @click="load(1)">search</el-button>
      <el-button type="warning" plain style="margin-left: 10px" @click="reset">reset</el-button>
    </div>

    <div class="operation">
      <el-button type="primary" plain @click="handleAdd">add</el-button>
      <el-button type="danger" plain @click="delBatch">delete in group</el-button>
    </div>

    <div class="table">
      <el-table :data="tableData" strip @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center"></el-table-column>
        <el-table-column prop="id" label="ID" width="70" align="center" sortable></el-table-column>
        <el-table-column prop="name" label="name"></el-table-column>
        <el-table-column prop="descr" label="describe" show-overflow-tooltip></el-table-column>
        <el-table-column prop="cover" label="cover">
          <template v-slot="scope">
            <div style="display: flex; align-items: center">
              <el-image style="width: 50px; height: 30px; border-radius: 5px" v-if="scope.row.cover"
                        :src="scope.row.cover" :preview-src-list="[scope.row.cover]"></el-image>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="start" label="start time"></el-table-column>
        <el-table-column prop="end" label="end time"></el-table-column>
        <el-table-column prop="form" label="activity form"></el-table-column>
        <el-table-column prop="address" label="activity address" show-overflow-tooltip></el-table-column>
        <el-table-column prop="host" label="host"></el-table-column>
        <el-table-column prop="readCount" label="views"></el-table-column>
        <el-table-column label="content">
          <template v-slot="scope">
            <el-button @click="preview(scope.row.content)">preview</el-button>
          </template>
        </el-table-column>
        <el-table-column label="actions" align="center" width="180">
          <template v-slot="scope">
            <el-button size="mini" type="primary" plain @click="handleEdit(scope.row)">edit</el-button>
            <el-button size="mini" type="danger" plain @click="del(scope.row.id)">delete</el-button>
          </template>
        </el-table-column>
      </el-table>


      <div class="pagination">
        <el-pagination
            background
            @current-change="handleCurrentChange"
            :current-page="pageNum"
            :page-sizes="[5, 10, 20]"
            :page-size="pageSize"
            layout="total, prev, pager, next"
            :total="total">
        </el-pagination>
      </div>
    </div>


    <el-dialog title="Information" :visible.sync="fromVisible" width="60%" :close-on-click-modal="false" destroy-on-close>
      <el-form :model="form" label-width="200px" style="padding-right: 50px" :rules="rules" ref="formRef">
        <el-form-item label="activity name" prop="name">
          <el-input v-model="form.name" placeholder="name"></el-input>
        </el-form-item>
        <el-form-item label="activity content" prop="descr">
          <el-input type="textarea" v-model="form.descr" placeholder="details"></el-input>
        </el-form-item>
        <el-form-item label="cover" prop="cover">
          <el-upload
              :action="$baseUrl + '/files/upload'"
              :headers="{ token: user.token }"
              list-type="picture"
              :on-success="handleCoverSuccess"
          >
            <el-button type="primary">Upload cover</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="start date" prop="start">
          <el-date-picker style="width: 100%" value-format="yyyy-MM-dd" format="yyyy-MM-dd" v-model="form.start" placeholder="开始时间"></el-date-picker>
        </el-form-item>
        <el-form-item label="end date" prop="end">
          <el-date-picker style="width: 100%" value-format="yyyy-MM-dd" format="yyyy-MM-dd" v-model="form.end" placeholder="结束时间"></el-date-picker>
        </el-form-item>
        <el-form-item label="activity form" prop="form">
          <el-select style="width: 100%" v-model="form.form">
            <el-option value="online"></el-option>
            <el-option value="offline"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="address" prop="address">
          <el-input v-model="form.address" placeholder="activity address"></el-input>
        </el-form-item>
        <el-form-item label="host" prop="host">
          <el-input v-model="form.host" placeholder="activity host"></el-input>
        </el-form-item>
        <el-form-item label="content" prop="content">
          <div id="editor"></div>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="fromVisible = false">cancel</el-button>
        <el-button type="primary" @click="save">submit</el-button>
      </div>
    </el-dialog>

    <el-dialog title="details" :visible.sync="fromVisible1" width="50%" :close-on-click-modal="false" :append-to-body="true" destroy-on-close>
      <div class="w-e-text">
        <div v-html="content"></div>
      </div>

      <div slot="footer" class="dialog-footer">
        <el-button @click="fromVisible1 = false">close</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import E from "wangeditor"
import hljs from 'highlight.js'

export default {
  name: "Activity",
  data() {
    return {
      tableData: [],  // 所有的数据
      pageNum: 1,   // 当前的页码
      pageSize: 10,  // 每页显示的个数
      total: 0,
      name: null,
      fromVisible: false,
      fromVisible1: false,
      form: {},
      user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
      rules: {
        name: [
          {required: true, message: 'please input activity name', trigger: 'blur'},
        ],
        descr: [
          {required: true, message: 'please input activity details', trigger: 'blur'},
        ],
        start: [
          {required: true, message: 'please input start date', trigger: 'blur'},
        ],
        end: [
          {required: true, message: 'please input end date', trigger: 'blur'},
        ],
        address: [
          {required: true, message: 'please input activity address', trigger: 'blur'},
        ],
        host: [
          {required: true, message: 'please input activity host', trigger: 'blur'},
        ],
      },
      ids: [],
      editor: null,
      content: ''
    }
  },
  created() {
    this.load(1)
  },
  methods: {
    preview(content){
      this.content = content
      this.fromVisible1 = true
    },
    handleAdd() {   // 新增数据
      this.form = {}  // 新增数据的时候清空数据
      this.fromVisible = true   // 打开弹窗
      this.setRichText('')
    },
    handleEdit(row) {   // 编辑数据
      this.form = JSON.parse(JSON.stringify(row))  // 给form对象赋值  注意要深拷贝数据
      this.fromVisible = true   // 打开弹窗
      this.setRichText(this.form.content)
    },
    save() {   // 保存按钮触发的逻辑  它会触发新增或者更新
      this.$refs.formRef.validate((valid) => {
        if (valid) {
          this.form.content = this.editor.txt.html()
          this.$request({
            url: this.form.id ? '/activity/update' : '/activity/add',
            method: this.form.id ? 'PUT' : 'POST',
            data: this.form
          }).then(res => {
            if (res.code === '200') {  // 表示成功保存
              this.$message.success('success')
              this.load(1)
              this.fromVisible = false
            } else {
              this.$message.error(res.msg)  // 弹出错误的信息
            }
          })
        }
      })
    },
    del(id) {   // 单个删除
      this.$confirm('are yous sure to delete it?', 'confirm', {type: "warning"}).then(response => {
        this.$request.delete('/activity/delete/' + id).then(res => {
          if (res.code === '200') {   // 表示操作成功
            this.$message.success('success')
            this.load(1)
          } else {
            this.$message.error(res.msg)  // 弹出错误的信息
          }
        })
      }).catch(() => {
      })
    },
    handleSelectionChange(rows) {   // 当前选中的所有的行数据
      this.ids = rows.map(v => v.id)   //  [1,2]
    },
    delBatch() {   // 批量删除
      if (!this.ids.length) {
        this.$message.warning('please select data')
        return
      }
      this.$confirm('are yous sure to delete them?', 'confirm', {type: "warning"}).then(response => {
        this.$request.delete('/activity/delete/batch', {data: this.ids}).then(res => {
          if (res.code === '200') {   // 表示操作成功
            this.$message.success('success')
            this.load(1)
          } else {
            this.$message.error(res.msg)  // 弹出错误的信息
          }
        })
      }).catch(() => {
      })
    },
    load(pageNum) {  // 分页查询
      if (pageNum) this.pageNum = pageNum
      this.$request.get('/activity/selectPage', {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          name: this.name,
        }
      }).then(res => {
        this.tableData = res.data?.list
        this.total = res.data?.total
      })
    },
    reset() {
      this.name = null
      this.load(1)
    },
    handleCurrentChange(pageNum) {
      this.load(pageNum)
    },
    handleCoverSuccess(res) {
      this.form.cover = res.data
    },
    setRichText(content) {
      this.$nextTick(() => {
        this.editor = new E(`#editor`)
        this.editor.highlight = hljs
        this.editor.config.uploadImgServer = this.$baseUrl + '/files/editor/upload'
        this.editor.config.uploadFileName = 'file'
        this.editor.config.uploadImgHeaders = {
          token: this.user.token
        }
        this.editor.config.uploadImgParams = {
          type: 'img',
        }
        this.editor.create()  // 创建
        this.editor.txt.html(content)
      })
    },
  }
}
</script>

<style scoped>

</style>
