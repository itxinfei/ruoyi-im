#!/usr/bin/env node

const { Server } = require('@modelcontextprotocol/sdk/server/index.js');
const { StdioServerTransport } = require('@modelcontextprotocol/sdk/server/stdio.js');
const {
  CallToolRequestSchema,
  ErrorCode,
  ListToolsRequestSchema,
  McpError,
} = require('@modelcontextprotocol/sdk/types.js');

class RuoYiIMValidator {
  constructor() {
    this.server = new Server(
      {
        name: 'ruoyi-im-validator',
        version: '1.0.0',
      },
      {
        capabilities: {
          tools: {},
        },
      }
    );

    this.setupToolHandlers();
  }

  setupToolHandlers() {
    this.server.setRequestHandler(ListToolsRequestSchema, async () => ({
      tools: [
        {
          name: 'validate_java_code',
          description: '验证 Java 代码是否符合 RuoYi-IM 规范',
          inputSchema: {
            type: 'object',
            properties: {
              code: {
                type: 'string',
                description: '要验证的 Java 代码',
              },
            },
            required: ['code'],
          },
        },
        {
          name: 'validate_vue_code',
          description: '验证 Vue 代码是否符合 RuoYi-IM 规范',
          inputSchema: {
            type: 'object',
            properties: {
              code: {
                type: 'string',
                description: '要验证的 Vue 代码',
              },
            },
            required: ['code'],
          },
        },
        {
          name: 'check_naming_convention',
          description: '检查命名规范是否符合项目标准',
          inputSchema: {
            type: 'object',
            properties: {
              name: {
                type: 'string',
                description: '要检查的名称',
              },
              type: {
                type: 'string',
                description: '名称类型 (class, method, variable, file)',
                enum: ['class', 'method', 'variable', 'file'],
              },
            },
            required: ['name', 'type'],
          },
        },
      ],
    }));

    this.server.setRequestHandler(CallToolRequestSchema, async (request) => {
      const { name, arguments: args } = request.params;

      try {
        switch (name) {
          case 'validate_java_code':
            return this.validateJavaCode(args.code);
          case 'validate_vue_code':
            return this.validateVueCode(args.code);
          case 'check_naming_convention':
            return this.checkNamingConvention(args.name, args.type);
          default:
            throw new McpError(
              ErrorCode.MethodNotFound,
              `Unknown tool: ${name}`
            );
        }
      } catch (error) {
        throw new McpError(
          ErrorCode.InternalError,
          `Tool execution failed: ${error.message}`
        );
      }
    });
  }

  validateJavaCode(code) {
    const issues = [];

    // 检查 JDK 1.8 语法
    if (code.includes('var ')) {
      issues.push({
        level: 'error',
        message: '使用了 JDK 10+ 的 var 关键字，请使用具体类型',
        line: this.findLineNumber(code, 'var '),
      });
    }

    if (code.includes('record ')) {
      issues.push({
        level: 'error',
        message: '使用了 JDK 14+ 的 record 类型，请使用 class',
        line: this.findLineNumber(code, 'record '),
      });
    }

    if (code.includes('switch ') && code.includes('->')) {
      issues.push({
        level: 'error',
        message: '使用了 JDK 14+ 的 switch 箭头语法，请使用传统 switch',
        line: this.findLineNumber(code, 'switch '),
      });
    }

    // 检查 System.out.println
    if (code.includes('System.out.println')) {
      issues.push({
        level: 'warning',
        message: '使用了 System.out.println，请使用 log 记录日志',
        line: this.findLineNumber(code, 'System.out.println'),
      });
    }

    // 检查 MyBatis Plus Lambda 查询
    if (code.includes('wrapper.eq("') || code.includes('wrapper.like("')) {
      issues.push({
        level: 'error',
        message: '使用了 Magic String，请使用 Lambda 查询 wrapper.eq(Entity::getField, value)',
        line: this.findLineNumber(code, 'wrapper.eq("'),
      });
    }

    return {
      content: [
        {
          type: 'text',
          text: JSON.stringify({ issues }, null, 2),
        },
      ],
    };
  }

  validateVueCode(code) {
    const issues = [];

    // 检查 script setup
    if (!code.includes('<script setup>') && !code.includes('setup')) {
      issues.push({
        level: 'warning',
        message: '建议使用 <script setup> 语法',
        line: 1,
      });
    }

    // 检查 scoped
    if (code.includes('<style') && !code.includes('scoped')) {
      issues.push({
        level: 'warning',
        message: 'CSS 样式应该添加 scoped 属性',
        line: this.findLineNumber(code, '<style'),
      });
    }

    // 检查 v-for key
    if (code.includes('v-for=') && !code.includes(':key=')) {
      issues.push({
        level: 'error',
        message: 'v-for 必须指定 key 属性',
        line: this.findLineNumber(code, 'v-for='),
      });
    }

    return {
      content: [
        {
          type: 'text',
          text: JSON.stringify({ issues }, null, 2),
        },
      ],
    };
  }

  checkNamingConvention(name, type) {
    const issues = [];

    switch (type) {
      case 'class':
        // Controller, Service, Entity 等
        if (!name.match(/^[A-Z][a-zA-Z0-9]*Controller$/) && 
            !name.match(/^[A-Z][a-zA-Z0-9]*Service$/) &&
            !name.match(/^[A-Z][a-zA-Z0-9]*Mapper$/)) {
          issues.push({
            level: 'warning',
            message: '类名应该遵循 PascalCase 规范，如 XxxController',
          });
        }
        break;
      case 'method':
        if (!name.match(/^[a-z][a-zA-Z0-9]*$/)) {
          issues.push({
            level: 'warning',
            message: '方法名应该遵循 camelCase 规范',
          });
        }
        break;
      case 'file':
        // Vue 文件
        if (name.endsWith('.vue') && !name.match(/^[A-Z][a-zA-Z0-9]*\.vue$/)) {
          issues.push({
            level: 'warning',
            message: 'Vue 文件名应该遵循 PascalCase 规范',
          });
        }
        break;
    }

    return {
      content: [
        {
          type: 'text',
          text: JSON.stringify({ issues }, null, 2),
        },
      ],
    };
  }

  findLineNumber(code, pattern) {
    const lines = code.split('\n');
    for (let i = 0; i < lines.length; i++) {
      if (lines[i].includes(pattern)) {
        return i + 1;
      }
    }
    return 1;
  }

  async run() {
    const transport = new StdioServerTransport();
    await this.server.connect(transport);
    console.error('RuoYi-IM Validator MCP server running on stdio');
  }
}

const server = new RuoYiIMValidator();
server.run().catch(console.error);