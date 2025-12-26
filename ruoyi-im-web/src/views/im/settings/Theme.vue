// 补充 Theme.vue 的样式部分
<style lang="scss" scoped>
.settings-theme {
  h3 {
    margin: 24px 0 16px;
    padding-bottom: 8px;
    border-bottom: 1px solid var(--border-lighter);
    color: var(--text-primary);

    &:first-child {
      margin-top: 0;
    }
  }

  .setting-hint {
    margin-left: 8px;
    color: var(--text-secondary);
    font-size: var(--font-size-small);
  }

  .background-preview {
    width: 200px;
    height: 120px;
    border: 1px dashed var(--border-color);
    border-radius: var(--border-radius);
    overflow: hidden;
    position: relative;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .background-actions {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background-color: rgba(0, 0, 0, 0.5);
      display: flex;
      align-items: center;
      justify-content: center;
      opacity: 0;
      transition: opacity var(--animation-duration);

      &:hover {
        opacity: 1;
      }

      .el-button {
        color: #fff;
      }
    }

    .background-upload {
      width: 100%;
      height: 100%;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      color: var(--text-secondary);
      transition: color var(--animation-duration);

      &:hover {
        color: var(--primary-color);
      }

      i {
        font-size: 24px;
        margin-bottom: 8px;
      }
    }
  }

  .theme-presets {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
    gap: 10px;

    .theme-preset {
      text-align: center;
      cursor: pointer;
      padding: 10px;
      border-radius: var(--border-radius);
      transition: background-color var(--animation-duration);

      &:hover {
        background-color: var(--background-color);
      }

      &.active {
        background-color: var(--border-lighter);
      }

      .preset-preview {
        width: 40px;
        height: 40px;
        border-radius: 50%;
        margin: 0 auto 8px;
      }

      .preset-name {
        font-size: var(--font-size-small);
        color: var(--text-regular);
      }
    }
  }
}
</style>
